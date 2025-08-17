package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作业提交服务类
 * 处理学生作业提交相关的业务逻辑
 */
@Service
@Transactional
public class SubmissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 提交作业
     * @param assignmentId 作业ID
     * @param content 提交内容
     * @param filePath 文件路径（可选）
     * @return 提交响应
     */
    public SubmissionResponse submitAssignment(UUID assignmentId, String content, String filePath) {
        logger.info("Processing assignment submission for assignment: {}", assignmentId);
        
        Long currentUserId = getCurrentUserId();
        
        // 验证作业存在
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        
        // 验证截止时间
        validateDeadline(assignment);
        
        // 检查重复提交
        checkDuplicateSubmission(assignmentId, currentUserId);
        
        // 创建提交记录
        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(currentUserId);
        submission.setContent(content);
        submission.setFilePath(filePath);
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());
        
        Submission savedSubmission = submissionRepository.save(submission);
        
        logger.info("Assignment submitted successfully. Submission ID: {}", savedSubmission.getId());
        
        return convertToSubmissionResponse(savedSubmission);
    }
    
    /**
     * 更新提交内容
     * @param submissionId 提交ID
     * @param content 新的提交内容
     * @param filePath 新的文件路径（可选）
     * @return 更新后的提交响应
     */
    public SubmissionResponse updateSubmission(UUID submissionId, String content, String filePath) {
        logger.info("Updating submission: {}", submissionId);
        
        Long currentUserId = getCurrentUserId();
        
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));
        
        // 验证权限：只有提交者本人可以修改
        if (!submission.getStudentId().equals(currentUserId)) {
            throw new AccessDeniedException("您只能修改自己的提交");
        }
        
        // 验证状态：只有未批改的提交可以修改
        if ("GRADED".equals(submission.getStatus()) || "RETURNED".equals(submission.getStatus())) {
            throw new IllegalStateException("已批改的提交不能修改");
        }
        
        // 验证截止时间
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        validateDeadline(assignment);
        
        // 更新提交内容
        submission.setContent(content);
        if (filePath != null) {
            submission.setFilePath(filePath);
        }
        submission.setUpdatedAt(LocalDateTime.now());
        
        Submission updatedSubmission = submissionRepository.save(submission);
        
        logger.info("Submission updated successfully: {}", submissionId);
        
        return convertToSubmissionResponse(updatedSubmission);
    }
    
    /**
     * 批改作业
     * @param submissionId 提交ID
     * @param score 分数
     * @param feedback 反馈
     * @return 批改后的提交响应
     */
    public SubmissionResponse gradeSubmission(UUID submissionId, Integer score, String feedback) {
        logger.info("Grading submission: {}", submissionId);
        
        Long currentUserId = getCurrentUserId();
        
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));
        
        // 验证权限：只有作业创建者可以批改
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        
        if (!assignment.getCreatorId().equals(currentUserId)) {
            throw new AccessDeniedException("您只能批改自己创建的作业");
        }
        
        // 验证状态：只有已提交的作业可以批改
        if (!"SUBMITTED".equals(submission.getStatus())) {
            throw new IllegalStateException("只能批改已提交状态的作业");
        }
        
        // 验证分数范围
        if (score != null && (score < 0 || score > assignment.getMaxScore())) {
            throw new IllegalArgumentException("分数必须在0到" + assignment.getMaxScore() + "之间");
        }
        
        // 更新批改信息
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus("GRADED");
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(currentUserId);
        submission.setUpdatedAt(LocalDateTime.now());
        
        Submission gradedSubmission = submissionRepository.save(submission);
        
        logger.info("Submission graded successfully: {}", submissionId);
        
        return convertToSubmissionResponse(gradedSubmission);
    }
    
    /**
     * 获取学生的提交记录
     * @param assignmentId 作业ID
     * @return 提交响应
     */
    public Optional<SubmissionResponse> getStudentSubmission(UUID assignmentId) {
        Long currentUserId = getCurrentUserId();
        
        Optional<Submission> submission = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUserId);
        
        return submission.map(this::convertToSubmissionResponse);
    }
    
    /**
     * 获取学生的所有提交记录
     * @return 提交列表
     */
    public List<SubmissionResponse> getStudentSubmissions() {
        Long currentUserId = getCurrentUserId();
        
        List<Submission> submissions = submissionRepository.findByStudentIdOrderBySubmittedAtDesc(currentUserId);
        
        return submissions.stream()
                .map(this::convertToSubmissionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取待批改的提交列表（教师用）
     * @return 待批改提交列表
     */
    public List<SubmissionResponse> getPendingGradeSubmissions() {
        Long currentUserId = getCurrentUserId();
        
        List<Submission> submissions = submissionRepository.findSubmissionsToGradeByTeacher(currentUserId);
        
        return submissions.stream()
                .map(this::convertToSubmissionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取提交详情
     * @param submissionId 提交ID
     * @return 提交响应
     */
    public SubmissionResponse getSubmissionById(UUID submissionId) {
        logger.info("Getting submission by id: {}", submissionId);
        
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));
        
        Long currentUserId = getCurrentUserId();
        
        // 验证权限：学生只能查看自己的提交，教师可以查看自己创建的作业的提交
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        
        boolean isStudent = submission.getStudentId().equals(currentUserId);
        boolean isTeacher = assignment.getCreatorId().equals(currentUserId);
        
        if (!isStudent && !isTeacher) {
            throw new AccessDeniedException("您只能查看自己的提交或自己创建的作业的提交");
        }
        
        return convertToSubmissionResponse(submission);
    }
    
    /**
     * 获取作业统计信息
     * @param assignmentId 作业ID
     * @return 统计信息映射
     */
    public java.util.Map<String, Object> getAssignmentStatistics(UUID assignmentId) {
        logger.info("Getting statistics for assignment: {}", assignmentId);
        
        // 验证作业存在
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        
        Long currentUserId = getCurrentUserId();
        
        // 验证权限：只有作业创建者可以查看统计
        if (!assignment.getCreatorId().equals(currentUserId)) {
            throw new AccessDeniedException("您只能查看自己创建的作业统计");
        }
        
        // 获取统计数据
        long totalSubmissions = submissionRepository.countByAssignmentId(assignmentId);
        long gradedSubmissions = submissionRepository.countByAssignmentIdAndStatus(assignmentId, "GRADED");
        long pendingSubmissions = submissionRepository.countByAssignmentIdAndStatus(assignmentId, "SUBMITTED");
        
        // 计算平均分
        List<Submission> gradedList = submissionRepository.findByAssignmentIdAndStatus(assignmentId, "GRADED");
        double averageScore = gradedList.stream()
                .filter(s -> s.getScore() != null)
                .mapToInt(Submission::getScore)
                .average()
                .orElse(0.0);
        
        java.util.Map<String, Object> statistics = new java.util.HashMap<>();
        statistics.put("assignmentId", assignmentId);
        statistics.put("assignmentTitle", assignment.getTitle());
        statistics.put("totalSubmissions", totalSubmissions);
        statistics.put("gradedSubmissions", gradedSubmissions);
        statistics.put("pendingSubmissions", pendingSubmissions);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        statistics.put("maxScore", assignment.getMaxScore());
        
        return statistics;
    }
    
    /**
     * 获取作业的统计信息
     * @param assignmentId 作业ID
     * @return 统计信息
     */
    public SubmissionStatistics getSubmissionStatistics(UUID assignmentId) {
        Long currentUserId = getCurrentUserId();
        
        // 验证权限：只有作业创建者可以查看统计
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        
        if (!assignment.getCreatorId().equals(currentUserId)) {
            throw new AccessDeniedException("您只能查看自己创建的作业统计");
        }
        
        long totalSubmissions = submissionRepository.countByAssignmentId(assignmentId);
        long gradedSubmissions = submissionRepository.countByAssignmentIdAndStatus(assignmentId, "GRADED");
        Double averageScore = submissionRepository.findAverageScoreByAssignmentId(assignmentId);
        Integer maxScore = submissionRepository.findMaxScoreByAssignmentId(assignmentId);
        Integer minScore = submissionRepository.findMinScoreByAssignmentId(assignmentId);
        
        return new SubmissionStatistics(totalSubmissions, gradedSubmissions, averageScore, maxScore, minScore);
    }
    
    /**
     * 验证截止时间
     * @param assignment 作业
     */
    private void validateDeadline(Assignment assignment) {
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            throw new IllegalStateException("作业已过截止时间，无法提交");
        }
    }
    
    /**
     * 检查重复提交
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     */
    private void checkDuplicateSubmission(UUID assignmentId, Long studentId) {
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            throw new IllegalStateException("您已经提交过此作业，请使用更新功能修改提交内容");
        }
    }
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("用户未登录");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("用户不存在"));
        
        return user.getId();
    }
    
    /**
     * 转换提交实体为响应DTO
     * @param submission 提交实体
     * @return 提交响应DTO
     */
    private SubmissionResponse convertToSubmissionResponse(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentId(submission.getAssignmentId());
        response.setStudentId(submission.getStudentId());
        response.setContent(submission.getContent());
        response.setFilePath(submission.getFilePath());
        response.setScore(submission.getScore());
        response.setFeedback(submission.getFeedback());
        response.setStatus(submission.getStatus());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        response.setCreatedAt(submission.getCreatedAt());
        response.setUpdatedAt(submission.getUpdatedAt());
        
        // 设置关联信息
        if (submission.getAssignment() != null) {
            response.setAssignmentTitle(submission.getAssignment().getTitle());
        }
        
        if (submission.getStudent() != null) {
            response.setStudentUsername(submission.getStudent().getUsername());
        }
        
        return response;
    }
    
    /**
     * 提交统计信息内部类
     */
    public static class SubmissionStatistics {
        private long totalSubmissions;
        private long gradedSubmissions;
        private Double averageScore;
        private Integer maxScore;
        private Integer minScore;
        
        public SubmissionStatistics(long totalSubmissions, long gradedSubmissions, 
                                  Double averageScore, Integer maxScore, Integer minScore) {
            this.totalSubmissions = totalSubmissions;
            this.gradedSubmissions = gradedSubmissions;
            this.averageScore = averageScore;
            this.maxScore = maxScore;
            this.minScore = minScore;
        }
        
        // Getter方法
        public long getTotalSubmissions() { return totalSubmissions; }
        public long getGradedSubmissions() { return gradedSubmissions; }
        public Double getAverageScore() { return averageScore; }
        public Integer getMaxScore() { return maxScore; }
        public Integer getMinScore() { return minScore; }
        public long getPendingSubmissions() { return totalSubmissions - gradedSubmissions; }
    }
}