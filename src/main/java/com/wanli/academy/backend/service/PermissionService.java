package com.wanli.academy.backend.service;

import com.wanli.academy.backend.entity.*;
import com.wanli.academy.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Permission service class
 * Responsible for handling user permission verification and access control
 */
@Service
@Transactional(readOnly = true)
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
     * Check if user has admin privileges
     * @return true if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Check if user has teacher privileges
     * @return true if user is teacher or admin
     */
    public boolean isTeacher() {
        return hasRole("ROLE_HQ_TEACHER") || hasRole("ROLE_FRANCHISE_TEACHER") || hasRole("ROLE_ADMIN");
    }

    /**
     * Check if user has student privileges
     * @return true if user is student, teacher or admin
     */
    public boolean isStudent() {
        logger.info("=== PermissionService.isStudent() called ===");
        boolean result = hasRole("ROLE_STUDENT");
        logger.info("isStudent() result: {}", result);
        return result;
    }

    /**
     * Check if user has specified role
     * @param roleName role name
     * @return true if user has the role
     */
    public boolean hasRole(String roleName) {
        logger.info("=== PermissionService.hasRole() called with roleName: {} ===", roleName);
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            logger.warn("getCurrentUser() returned null");
            return false;
        }
        
        logger.info("Current user: {}, roles count: {}", currentUser.getUsername(), currentUser.getRoles().size());
        for (Role role : currentUser.getRoles()) {
            logger.info("User role: {}", role.getName());
        }
        
        boolean result = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
        logger.info("hasRole({}) result: {}", roleName, result);
        return result;
    }

    /**
     * Check if user can access specified assignment
     * @param assignmentId assignment ID
     * @return true if user can access the assignment
     */
    public boolean canAccessAssignment(UUID assignmentId) {
        System.out.println("=== PermissionService.canAccessAssignment() called ===");
        System.out.println("Assignment ID: " + assignmentId);
        logger.info("=== PermissionService.canAccessAssignment() called for assignmentId: {} ===", assignmentId);
        logger.info("=== STACK TRACE ===");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
            logger.info("Stack[{}]: {}", i, stackTrace[i]);
        }
        logger.info("=== END STACK TRACE ===");
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            logger.warn("canAccessAssignment: No current user found");
            return false;
        }
        
        logger.info("canAccessAssignment: Current user: {}, roles: {}", currentUser.getUsername(), currentUser.getRoles());
        
        // Admin and teachers can access all assignments
        if (isAdmin() || isTeacher()) {
            logger.info("canAccessAssignment: User is admin or teacher, access granted");
            return true;
        }
        
        // Students can only access published assignments
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) {
            logger.warn("canAccessAssignment: Assignment not found: {}", assignmentId);
            return false;
        }
        
        Assignment assignment = assignmentOpt.get();
        boolean isPublished = "PUBLISHED".equals(assignment.getStatus());
        logger.info("canAccessAssignment: Assignment status: {}, isPublished: {}", assignment.getStatus(), isPublished);
        
        boolean result = isPublished;
        logger.info("canAccessAssignment: Final result: {}", result);
        return result;
    }

    /**
     * Check if user can modify specified assignment
     * @param assignmentId assignment ID
     * @return true if user can modify the assignment
     */
    public boolean canModifyAssignment(UUID assignmentId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // Admin can modify all assignments
            if (isAdmin()) {
                return true;
            }

            // Teachers can only modify assignments they created
            if (isTeacher()) {
                Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
                return assignment.isPresent() && 
                       assignment.get().getCreatorId().equals(currentUser.getId());
            }

            return false;
        } catch (Exception e) {
            logger.error("Error occurred while checking assignment modification permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can access specified submission
     * @param submissionId submission ID
     * @return true if user can access the submission
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

            // Admin and teachers can access all submissions
            if (isAdmin() || isTeacher()) {
                return true;
            }

            // Students can only access their own submissions
            return sub.getStudentId().equals(currentUser.getId());
        } catch (Exception e) {
            logger.error("Error occurred while checking submission access permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can grade specified submission
     * @param submissionId submission ID
     * @return true if user can grade the submission
     */
    public boolean canGradeSubmission(UUID submissionId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // Only admin and teachers can grade
            if (!isAdmin() && !isTeacher()) {
                return false;
            }

            Optional<Submission> submission = submissionRepository.findById(submissionId);
            if (!submission.isPresent()) {
                return false;
            }

            // Admin can grade all submissions
            if (isAdmin()) {
                return true;
            }

            // Teachers can only grade submissions for assignments they created
            Submission sub = submission.get();
            Optional<Assignment> assignment = assignmentRepository.findById(sub.getAssignmentId());
            return assignment.isPresent() && 
                   assignment.get().getCreatorId().equals(currentUser.getId());
        } catch (Exception e) {
            logger.error("Error occurred while checking submission grading permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can delete submission
     */
    public boolean canDeleteSubmission(UUID submissionId) {
        if (isAdmin()) {
            return true;
        }
        
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return false;
        }
        
        // Students can only delete their own submissions
        return isStudent() && getCurrentUserId().equals(submission.getStudentId());
    }

    /**
     * Check if user can access file
     */
    public boolean canAccessFile(UUID fileId) {
        // First check if file exists
        AssignmentFile file = assignmentFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return false;
        }
        
        // Admin can access all existing files
        if (isAdmin()) {
            return true;
        }
        
        // File uploader can access
        if (getCurrentUserId().equals(file.getUploadedBy())) {
            return true;
        }
        
        // If file is associated with assignment, check assignment access permission
        if (file.getAssignmentId() != null) {
            return canAccessAssignment(file.getAssignmentId());
        }
        
        return false;
    }

    /**
     * Check if user can delete specified file
     * @param fileId file ID
     * @param uploaderId file uploader ID
     * @return true if user can delete the file
     */
    public boolean canDeleteFile(UUID fileId, Long uploaderId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // Admin can delete all files
            if (isAdmin()) {
                return true;
            }

            // Users can only delete files they uploaded
            return currentUser.getId().equals(uploaderId);
        } catch (Exception e) {
            logger.error("Error occurred while checking file deletion permission: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can delete file
     */
    public boolean canDeleteFile(UUID fileId) {
        if (isAdmin()) {
            return true;
        }
        
        AssignmentFile file = assignmentFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            return false;
        }
        
        // File uploader can delete
        if (getCurrentUserId().equals(file.getUploadedBy())) {
            return true;
        }
        
        // Teachers can delete assignment-related files
        if (isTeacher() && file.getAssignmentId() != null) {
            return canModifyAssignment(file.getAssignmentId());
        }
        
        return false;
    }

    /**
     * Get current logged-in user
     * @return current user object, null if not logged in
     */
    private User getCurrentUser() {
        try {
            logger.info("=== getCurrentUser() called");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.info("=== authentication: {}", authentication);
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.info("=== authentication is null or not authenticated");
                return null;
            }

            String username = authentication.getName();
            logger.info("=== username from authentication: {}", username);
            if ("anonymousUser".equals(username)) {
                logger.info("=== username is anonymousUser");
                return null;
            }

            User user = userRepository.findByUsername(username).orElse(null);
            logger.info("=== user found in database: {}", user != null ? user.getUsername() : "null");
            if (user != null) {
                logger.info("=== user roles: {}", user.getRoles().stream().map(Role::getName).toArray());
            }
            return user;
        } catch (Exception e) {
            logger.error("Error occurred while getting current user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current user ID
     * @return current user ID, null if not logged in
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * Get current username
     * @return current username, null if not logged in
     */
    public String getCurrentUsername() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : null;
    }
}