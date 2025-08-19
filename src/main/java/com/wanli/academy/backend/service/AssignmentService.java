package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AssignmentCreateRequest;
import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.AssignmentFileResponse;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.AssignmentFile;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import com.wanli.academy.backend.repository.AssignmentFileRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作业服务类
 * 处理作业相关的CRUD业务逻辑
 * 注意：查询相关方法已迁移到 AssignmentServiceQuery.java
 */
@Service
@Transactional
public class AssignmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private AssignmentFileRepository assignmentFileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 创建新作业
     * @param request 创建作业请求
     * @return 创建的作业响应
     */
    public AssignmentResponse createAssignment(AssignmentCreateRequest request) {
        logger.info("Creating new assignment with title: {}", request.getTitle());
        
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 创建作业实体
        Assignment assignment = new Assignment();
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setCreatorId(currentUser.getId());
        assignment.setCreator(currentUser);
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxScore(request.getTotalScore());
        assignment.setStatus(request.getStatus());
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // 保存作业
        Assignment savedAssignment = assignmentRepository.save(assignment);
        
        logger.info("Successfully created assignment with ID: {}", savedAssignment.getId());
        
        return convertToAssignmentResponse(savedAssignment);
    }
    
    /**
     * 更新作业状态
     * @param assignmentId 作业ID
     * @param newStatus 新状态
     * @return 更新后的作业响应
     */
    public AssignmentResponse updateAssignmentStatus(UUID assignmentId, String newStatus) {
        logger.info("Updating assignment status - ID: {}, new status: {}", assignmentId, newStatus);
        
        // 验证作业存在且属于当前用户
        Assignment assignment = validateAssignmentOwnership(assignmentId);
        
        // 验证状态转换的合法性
        validateStatusTransition(assignment.getStatus(), newStatus);
        
        // 更新状态
        assignment.setStatus(newStatus);
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        
        logger.info("Successfully updated assignment status - ID: {}, status: {} -> {}", 
                   assignmentId, assignment.getStatus(), newStatus);
        
        return convertToAssignmentResponse(updatedAssignment);
    }
    
    /**
     * 验证状态转换的合法性
     * @param currentStatus 当前状态
     * @param newStatus 新状态
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // 定义合法的状态转换规则
        boolean isValidTransition = false;
        
        switch (currentStatus) {
            case "DRAFT":
                // 草稿可以转换为已发布或保持草稿
                isValidTransition = "PUBLISHED".equals(newStatus) || "DRAFT".equals(newStatus);
                break;
            case "PUBLISHED":
                // 已发布可以转换为已关闭或保持已发布
                isValidTransition = "CLOSED".equals(newStatus) || "PUBLISHED".equals(newStatus);
                break;
            case "CLOSED":
                // 已关闭只能保持已关闭状态
                isValidTransition = "CLOSED".equals(newStatus);
                break;
        }
        
        if (!isValidTransition) {
            logger.warn("Invalid status transition: {} -> {}", currentStatus, newStatus);
            throw new RuntimeException(String.format("无效的状态转换：%s -> %s", currentStatus, newStatus));
        }
    }
    
    /**
     * 更新作业信息
     * @param assignmentId 作业ID
     * @param request 更新请求
     * @return 更新后的作业响应
     */
    public AssignmentResponse updateAssignment(UUID assignmentId, AssignmentCreateRequest request) {
        logger.info("Updating assignment with ID: {}", assignmentId);
        
        // 验证作业存在且属于当前用户
        Assignment assignment = validateAssignmentOwnership(assignmentId);
        
        // 更新作业信息
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxScore(request.getTotalScore());
        assignment.setStatus(request.getStatus());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        
        logger.info("Successfully updated assignment with ID: {}", assignmentId);
        
        return convertToAssignmentResponse(updatedAssignment);
    }
    
    /**
     * 删除作业
     * @param assignmentId 作业ID
     */
    public void deleteAssignment(UUID assignmentId) {
        logger.info("Deleting assignment with ID: {}", assignmentId);
        
        // 验证作业存在且属于当前用户
        Assignment assignment = validateAssignmentOwnership(assignmentId);
        
        // 检查是否有提交记录
        long submissionCount = submissionRepository.countByAssignmentId(assignmentId);
        if (submissionCount > 0) {
            logger.warn("Cannot delete assignment {} with {} submissions", assignmentId, submissionCount);
            throw new RuntimeException("无法删除已有提交记录的作业");
        }
        
        // 删除相关文件记录
        assignmentFileRepository.deleteByAssignmentId(assignmentId);
        
        // 删除作业
        assignmentRepository.delete(assignment);
        
        logger.info("Successfully deleted assignment with ID: {}", assignmentId);
    }
    
    /**
     * 验证作业所有权
     * @param assignmentId 作业ID
     * @return 作业实体
     * @throws RuntimeException 如果作业不存在
     * @throws AccessDeniedException 如果用户无权访问该作业
     */
    public Assignment validateAssignmentOwnership(UUID assignmentId) {
        logger.debug("Validating assignment ownership for ID: {}", assignmentId);
        
        // 查找作业
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.warn("Assignment not found with ID: {}", assignmentId);
                    return new RuntimeException("作业不存在");
                });
        
        // 获取当前用户
        User currentUser = getCurrentUser();
        
        // 验证所有权
        if (!assignment.getCreator().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to access assignment {} owned by user {}", 
                       currentUser.getUsername(), assignmentId, assignment.getCreator().getUsername());
            throw new AccessDeniedException("您无权访问此作业");
        }
        
        return assignment;
    }
    
    /**
     * 获取当前登录用户
     * @return 当前用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Current user not found: {}", username);
                    return new RuntimeException("当前用户不存在");
                });
    }
    
    /**
     * 转换作业实体为响应DTO
     * @param assignment 作业实体
     * @return 作业响应DTO
     */
    private AssignmentResponse convertToAssignmentResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setCreatorId(assignment.getCreator().getId());
        response.setCreatorUsername(assignment.getCreator().getUsername());
        response.setDueDate(assignment.getDueDate());
        response.setTotalScore(assignment.getMaxScore());
        response.setStatus(assignment.getStatus());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        
        // 如果需要包含提交列表，可以在这里添加
        if (assignment.getSubmissions() != null) {
            List<SubmissionResponse> submissionResponses = assignment.getSubmissions().stream()
                    .map(this::convertToSubmissionResponse)
                    .collect(Collectors.toList());
            response.setSubmissions(submissionResponses);
        }
        
        // 如果需要包含文件列表，可以在这里添加
        if (assignment.getFiles() != null) {
            List<AssignmentFileResponse> fileResponses = assignment.getFiles().stream()
                    .map(this::convertToAssignmentFileResponse)
                    .collect(Collectors.toList());
            response.setFiles(fileResponses);
        }
        
        return response;
    }
    
    /**
     * 转换提交实体为响应DTO
     * @param submission 提交实体
     * @return 提交响应DTO
     */
    private SubmissionResponse convertToSubmissionResponse(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentId(submission.getAssignment().getId());
        response.setAssignmentTitle(submission.getAssignment().getTitle());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentUsername(submission.getStudent().getUsername());
        response.setContent(submission.getContent());
        response.setFilePath(submission.getFilePath());
        response.setScore(submission.getScore());
        response.setFeedback(submission.getFeedback());
        response.setStatus(submission.getStatus());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        response.setCreatedAt(submission.getCreatedAt());
        response.setUpdatedAt(submission.getUpdatedAt());
        
        return response;
    }
    
    /**
     * 转换文件实体为响应DTO
     * @param file 文件实体
     * @return 文件响应DTO
     */
    private AssignmentFileResponse convertToAssignmentFileResponse(AssignmentFile file) {
        AssignmentFileResponse response = new AssignmentFileResponse();
        response.setId(file.getId());
        response.setAssignmentId(file.getAssignment().getId());
        response.setAssignmentTitle(file.getAssignment().getTitle());
        response.setFileName(file.getFileName());
        response.setFilePath(file.getFilePath());
        response.setFileSize(file.getFileSize());
        response.setFileType(file.getFileType());
        response.setFileCategory(file.getFileCategory());
        response.setUploaderId(file.getUploader().getId());
        response.setUploaderUsername(file.getUploader().getUsername());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());
        
        return response;
    }
}
// 注意：以下方法已迁移到 AssignmentServiceQuery.java：
// - getAssignmentsByCreator()
// - getAssignmentsByCreatorWithPagination(Pageable pageable)
// - getAssignmentsByStatus(String status)
// - getAssignmentsByStatusWithPagination(String status, Pageable pageable)
// - getAssignmentsWithFilters(Long creatorId, String status, String title, Pageable pageable)
// - getAssignmentById(UUID assignmentId)
// - getSubmissionsByAssignment(UUID assignmentId)
// - getFilesByAssignment(UUID assignmentId)
// - getAssignmentsDueSoon(int hours)