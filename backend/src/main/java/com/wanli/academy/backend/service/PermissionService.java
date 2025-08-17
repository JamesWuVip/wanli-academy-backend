package com.wanli.academy.backend.service;

import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.AssignmentFile;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.AssignmentFileRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 权限验证服务
 * 提供基于角色和资源的访问控制逻辑
 */
@Service
public class PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentFileRepository assignmentFileRepository;

    /**
     * 检查用户是否有管理员权限
     * @return true如果用户是管理员
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查用户是否有教师权限
     * @return true如果用户是教师或管理员
     */
    public boolean isTeacher() {
        return hasRole("HQ_TEACHER") || hasRole("FRANCHISE_TEACHER") || hasRole("ADMIN");
    }

    /**
     * 检查用户是否有学生权限
     * @return true如果用户是学生、教师或管理员
     */
    public boolean isStudent() {
        return hasRole("STUDENT") || hasRole("HQ_TEACHER") || hasRole("FRANCHISE_TEACHER") || hasRole("ADMIN");
    }

    /**
     * 检查用户是否具有指定角色
     * @param roleName 角色名称
     * @return true如果用户具有该角色
     */
    public boolean hasRole(String roleName) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            Set<Role> roles = currentUser.getRoles();
            return roles.stream()
                    .anyMatch(role -> role.getName().equals("ROLE_" + roleName) || 
                                    role.getName().equals(roleName));
        } catch (Exception e) {
            logger.error("检查用户角色时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以访问指定作业
     * @param assignmentId 作业ID
     * @return true如果用户可以访问该作业
     */
    public boolean canAccessAssignment(UUID assignmentId) {
        logger.info("=== canAccessAssignment called with ID: {}", assignmentId);
        
        User currentUser = getCurrentUser();
        logger.info("=== currentUser: {}", currentUser != null ? currentUser.getUsername() : "null");
        if (currentUser == null) {
            logger.info("=== 当前用户为空，返回false");
            return false;
        }

        // 首先检查作业是否存在
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        logger.info("=== assignment.isPresent(): {}", assignment.isPresent());
        if (!assignment.isPresent()) {
            logger.info("=== 作业不存在，返回false");
            return false;
        }

        // 管理员和教师可以访问所有存在的作业
        boolean isAdminResult = isAdmin();
        boolean isTeacherResult = isTeacher();
        logger.info("=== isAdmin(): {}, isTeacher(): {}", isAdminResult, isTeacherResult);
        if (isAdminResult || isTeacherResult) {
            logger.info("=== 管理员或教师，返回true");
            return true;
        }

        // 学生只能访问已发布的作业
        boolean canAccess = "PUBLISHED".equals(assignment.get().getStatus());
        logger.info("=== 学生访问，状态: {}, 结果: {}", assignment.get().getStatus(), canAccess);
        return canAccess;
    }

    /**
     * 检查用户是否可以修改指定作业
     * @param assignmentId 作业ID
     * @return true如果用户可以修改该作业
     */
    public boolean canModifyAssignment(UUID assignmentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // 管理员可以修改所有作业
            if (isAdmin()) {
                return true;
            }

            // 教师只能修改自己创建的作业
            if (isTeacher()) {
                Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
                return assignment.isPresent() && 
                       assignment.get().getCreatorId().equals(currentUser.getId());
            }

            return false;
        } catch (Exception e) {
            logger.error("检查作业修改权限时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以访问指定提交
     * @param submissionId 提交ID
     * @return true如果用户可以访问该提交
     */
    public boolean canAccessSubmission(UUID submissionId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            Optional<Submission> submission = submissionRepository.findById(submissionId);
            if (!submission.isPresent()) {
                return false;
            }

            Submission sub = submission.get();

            // 管理员和教师可以访问所有提交
            if (isAdmin() || isTeacher()) {
                return true;
            }

            // 学生只能访问自己的提交
            return sub.getStudentId().equals(currentUser.getId());
        } catch (Exception e) {
            logger.error("检查提交访问权限时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以评分指定提交
     * @param submissionId 提交ID
     * @return true如果用户可以评分该提交
     */
    public boolean canGradeSubmission(UUID submissionId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // 只有管理员和教师可以评分
            if (!isAdmin() && !isTeacher()) {
                return false;
            }

            Optional<Submission> submission = submissionRepository.findById(submissionId);
            if (!submission.isPresent()) {
                return false;
            }

            // 管理员可以评分所有提交
            if (isAdmin()) {
                return true;
            }

            // 教师只能评分自己创建的作业的提交
            Submission sub = submission.get();
            Optional<Assignment> assignment = assignmentRepository.findById(sub.getAssignmentId());
            return assignment.isPresent() && 
                   assignment.get().getCreatorId().equals(currentUser.getId());
        } catch (Exception e) {
            logger.error("检查提交评分权限时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以删除提交
     */
    public boolean canDeleteSubmission(UUID submissionId) {
        if (isAdmin()) {
            return true;
        }
        
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return false;
        }
        
        // 学生只能删除自己的提交
        return isStudent() && getCurrentUserId().equals(submission.getStudentId());
    }

    /**
     * 检查用户是否可以访问文件
     */
    public boolean canAccessFile(UUID fileId) {
        // 首先检查文件是否存在
        AssignmentFile file = assignmentFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return false;
        }
        
        // 管理员可以访问所有存在的文件
        if (isAdmin()) {
            return true;
        }
        
        // 文件上传者可以访问
        if (getCurrentUserId().equals(file.getUploadedBy())) {
            return true;
        }
        
        // 如果文件关联了作业，检查作业访问权限
        if (file.getAssignmentId() != null) {
            return canAccessAssignment(file.getAssignmentId());
        }
        
        return false;
    }

    /**
     * 检查用户是否可以删除指定文件
     * @param fileId 文件ID
     * @param uploaderId 文件上传者ID
     * @return true如果用户可以删除该文件
     */
    public boolean canDeleteFile(UUID fileId, Long uploaderId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // 管理员可以删除所有文件
            if (isAdmin()) {
                return true;
            }

            // 用户只能删除自己上传的文件
            return currentUser.getId().equals(uploaderId);
        } catch (Exception e) {
            logger.error("检查文件删除权限时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否可以删除文件
     */
    public boolean canDeleteFile(UUID fileId) {
        if (isAdmin()) {
            return true;
        }
        
        AssignmentFile file = assignmentFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return false;
        }
        
        // 文件上传者可以删除
        if (getCurrentUserId().equals(file.getUploadedBy())) {
            return true;
        }
        
        // 教师可以删除作业相关文件
        if (isTeacher() && file.getAssignmentId() != null) {
            return canModifyAssignment(file.getAssignmentId());
        }
        
        return false;
    }

    /**
     * 获取当前登录用户
     * @return 当前用户对象，如果未登录则返回null
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String username = authentication.getName();
            if ("anonymousUser".equals(username)) {
                return null;
            }

            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            logger.error("获取当前用户时发生错误: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果未登录则返回null
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 获取当前用户名
     * @return 当前用户名，如果未登录则返回null
     */
    public String getCurrentUsername() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : null;
    }
}