package com.wanli.academy.backend.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作业查询服务类
 * 处理作业相关的查询业务逻辑
 */
@Service
@Transactional(readOnly = true)
public class AssignmentServiceQuery {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceQuery.class);
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private AssignmentFileRepository assignmentFileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取当前用户创建的作业列表
     * @return 作业列表
     */
    public List<AssignmentResponse> getAssignmentsByCreator() {
        logger.info("Fetching assignments for current user");
        
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 查询当前用户创建的作业
        List<Assignment> assignments = assignmentRepository.findByCreatorIdOrderByCreatedAtDesc(currentUser.getId());
        
        logger.info("Found {} assignments for user: {}", assignments.size(), currentUser.getUsername());
        
        return assignments.stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页获取当前用户创建的作业列表
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    public Page<AssignmentResponse> getAssignmentsByCreatorWithPagination(Pageable pageable) {
        logger.info("Fetching assignments for current user with pagination: page={}, size={}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 分页查询当前用户创建的作业
        Page<Assignment> assignmentPage = assignmentRepository.findByCreatorIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        
        logger.info("Found {} assignments for user: {} (page {} of {})", 
                   assignmentPage.getNumberOfElements(), currentUser.getUsername(),
                   assignmentPage.getNumber() + 1, assignmentPage.getTotalPages());
        
        return assignmentPage.map(this::convertToAssignmentResponse);
    }
    
    /**
     * 根据状态获取作业列表
     * @param status 作业状态
     * @return 作业列表
     */
    public List<AssignmentResponse> getAssignmentsByStatus(String status) {
        logger.info("Fetching assignments with status: {}", status);
        
        List<Assignment> assignments = assignmentRepository.findByStatusOrderByCreatedAtDesc(status);
        
        logger.info("Found {} assignments with status: {}", assignments.size(), status);
        
        return assignments.stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页根据状态获取作业列表
     * @param status 作业状态
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    public Page<AssignmentResponse> getAssignmentsByStatusWithPagination(String status, Pageable pageable) {
        logger.info("Fetching assignments with status: {} with pagination: page={}, size={}", 
                   status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Assignment> assignmentPage = assignmentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        
        logger.info("Found {} assignments with status: {} (page {} of {})", 
                   assignmentPage.getNumberOfElements(), status,
                   assignmentPage.getNumber() + 1, assignmentPage.getTotalPages());
        
        return assignmentPage.map(this::convertToAssignmentResponse);
    }
    
    /**
     * 综合筛选和分页查询作业列表
     * @param creatorId 创建者ID（可选）
     * @param status 作业状态（可选）
     * @param title 标题关键词（可选）
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    public Page<AssignmentResponse> getAssignmentsWithFilters(Long creatorId, String status, String title, Pageable pageable) {
        logger.info("Fetching assignments with filters - creatorId: {}, status: {}, title: {}, page: {}, size: {}", 
                   creatorId, status, title, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Assignment> assignmentPage;
        
        if (creatorId != null && status != null && title != null) {
            // 三个条件都有
            assignmentPage = assignmentRepository.findByCreatorIdAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    creatorId, status, title, pageable);
        } else if (creatorId != null && status != null) {
            // 创建者和状态
            assignmentPage = assignmentRepository.findByCreatorIdAndStatusOrderByCreatedAtDesc(creatorId, status, pageable);
        } else if (creatorId != null && title != null) {
            // 创建者和标题
            assignmentPage = assignmentRepository.findByCreatorIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    creatorId, title, pageable);
        } else if (status != null && title != null) {
            // 状态和标题
            assignmentPage = assignmentRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                    status, title, pageable);
        } else if (creatorId != null) {
            // 仅创建者
            assignmentPage = assignmentRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId, pageable);
        } else if (status != null) {
            // 仅状态
            assignmentPage = assignmentRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else if (title != null) {
            // 仅标题
            assignmentPage = assignmentRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title, pageable);
        } else {
            // 无筛选条件，返回所有
            assignmentPage = assignmentRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        logger.info("Found {} assignments with filters (page {} of {})", 
                   assignmentPage.getNumberOfElements(),
                   assignmentPage.getNumber() + 1, assignmentPage.getTotalPages());
        
        return assignmentPage.map(this::convertToAssignmentResponse);
    }
    
    /**
     * 根据ID获取作业详情
     * @param assignmentId 作业ID
     * @return 作业响应
     */
    public AssignmentResponse getAssignmentById(UUID assignmentId) {
        logger.info("Fetching assignment with ID: {}", assignmentId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.warn("Assignment not found with ID: {}", assignmentId);
                    return new RuntimeException("作业不存在");
                });
        
        return convertToAssignmentResponse(assignment);
    }
    
    /**
     * 获取作业的提交列表
     * @param assignmentId 作业ID
     * @return 提交列表
     */
    public List<SubmissionResponse> getSubmissionsByAssignment(UUID assignmentId) {
        logger.info("Fetching submissions for assignment: {}", assignmentId);
        
        // 验证作业存在
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.warn("Assignment not found with ID: {}", assignmentId);
                    return new RuntimeException("作业不存在");
                });
        
        List<Submission> submissions = submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
        
        logger.info("Found {} submissions for assignment: {}", submissions.size(), assignmentId);
        
        return submissions.stream()
                .map(this::convertToSubmissionResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取作业的文件列表
     * @param assignmentId 作业ID
     * @return 文件列表
     */
    public List<AssignmentFileResponse> getFilesByAssignment(UUID assignmentId) {
        logger.info("Fetching files for assignment: {}", assignmentId);
        
        // 验证作业存在
        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> {
                    logger.warn("Assignment not found with ID: {}", assignmentId);
                    return new RuntimeException("作业不存在");
                });
        
        List<AssignmentFile> files = assignmentFileRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
        
        logger.info("Found {} files for assignment: {}", files.size(), assignmentId);
        
        return files.stream()
                .map(this::convertToAssignmentFileResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取即将到期的作业列表
     * @param hours 小时数
     * @return 作业列表
     */
    public List<AssignmentResponse> getAssignmentsDueSoon(int hours) {
        logger.info("Fetching assignments due within {} hours", hours);
        
        List<Assignment> assignments = assignmentRepository.findAssignmentsDueSoon(hours);
        
        logger.info("Found {} assignments due soon", assignments.size());
        
        return assignments.stream()
                .map(this::convertToAssignmentResponse)
                .collect(Collectors.toList());
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