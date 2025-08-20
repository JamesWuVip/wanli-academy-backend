package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AssignmentRequest;
import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.AssignmentSubmissionRequest;
import com.wanli.academy.backend.dto.AssignmentSubmissionResponse;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.AssignmentSubmission;
import com.wanli.academy.backend.entity.Course;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.AssignmentSubmissionRepository;
import com.wanli.academy.backend.repository.CourseRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Assignment Service
 * Handles assignment-related CRUD business logic
 */
@Service
@Transactional
public class AssignmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private AssignmentSubmissionRepository submissionRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Create assignment
     * @param assignmentRequest assignment creation request
     * @param username creator username
     * @return assignment response
     * @throws RuntimeException when course not found or user has no permission
     */
    public AssignmentResponse createAssignment(AssignmentRequest assignmentRequest, String username) {
        logger.info("Creating assignment, title: {}, creator: {}", assignmentRequest.getTitle(), username);
        
        // Get creator user information
        User creator = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get course information
        Course course = courseRepository.findById(assignmentRequest.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Check if user has permission to create assignment in this course
        if (!hasPermissionToManageAssignment(creator, course)) {
            logger.warn("User {} does not have permission to create assignment in course {}", username, course.getId());
            throw new RuntimeException("No permission to create assignment in this course");
        }
        
        // Create assignment entity
        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentRequest.getTitle());
        assignment.setDescription(assignmentRequest.getDescription());
        assignment.setInstructions(assignmentRequest.getInstructions());
        assignment.setDueDate(assignmentRequest.getDueDate());
        assignment.setMaxScore(assignmentRequest.getMaxScore());
        assignment.setIsActive(true);
        assignment.setCourse(course);
        assignment.setCreatedBy(creator);
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // Save assignment
        Assignment savedAssignment = assignmentRepository.save(assignment);
        logger.info("Assignment created successfully, ID: {}", savedAssignment.getId());
        
        return convertToAssignmentResponse(savedAssignment);
    }
    
    /**
     * Get assignment list
     * @param pageable pagination information
     * @param courseId course ID filter (optional)
     * @param status assignment status filter (optional)
     * @param username current user username
     * @return assignment page
     */
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getAssignments(Pageable pageable, Long courseId, String status, String username) {
        logger.info("Getting assignment list, courseId: {}, status: {}, user: {}", courseId, status, username);
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Build query conditions
        Specification<Assignment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only show active assignments
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));
            
            // Course filter
            if (courseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("course").get("id"), courseId));
            }
            
            // Status filter
            if (status != null && !status.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                switch (status.toLowerCase()) {
                    case "active":
                        predicates.add(criteriaBuilder.greaterThan(root.get("dueDate"), now));
                        break;
                    case "expired":
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), now));
                        break;
                }
            }
            
            // For students, only show assignments from courses they are enrolled in
            if (isStudent(currentUser)) {
                // This would require a join with course enrollment, simplified for now
                // predicates.add(criteriaBuilder.in(root.get("course")).value(getEnrolledCourses(currentUser)));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Assignment> assignmentPage = assignmentRepository.findAll(spec, pageable);
        
        return assignmentPage.map(this::convertToAssignmentResponse);
    }
    
    /**
     * Get assignment details by ID
     * @param id assignment ID
     * @param username current user username
     * @return assignment response
     * @throws RuntimeException when assignment not found or user has no permission
     */
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long id, String username) {
        logger.info("Getting assignment details, ID: {}, user: {}", id, username);
        
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has permission to view this assignment
        if (!hasPermissionToViewAssignment(currentUser, assignment)) {
            logger.warn("User {} does not have permission to view assignment {}", username, id);
            throw new RuntimeException("No permission to view this assignment");
        }
        
        return convertToAssignmentResponse(assignment);
    }
    
    /**
     * Update assignment
     * @param id assignment ID
     * @param assignmentRequest assignment update request
     * @param username current user username
     * @return updated assignment response
     * @throws RuntimeException when assignment not found or user has no permission
     */
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest assignmentRequest, String username) {
        logger.info("Updating assignment, ID: {}, user: {}", id, username);
        
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has permission to update this assignment
        if (!hasPermissionToManageAssignment(currentUser, assignment.getCourse()) && 
            !assignment.getCreatedBy().getId().equals(currentUser.getId())) {
            logger.warn("User {} does not have permission to update assignment {}", username, id);
            throw new RuntimeException("No permission to update this assignment");
        }
        
        // Update assignment information
        assignment.setTitle(assignmentRequest.getTitle());
        assignment.setDescription(assignmentRequest.getDescription());
        assignment.setInstructions(assignmentRequest.getInstructions());
        assignment.setDueDate(assignmentRequest.getDueDate());
        assignment.setMaxScore(assignmentRequest.getMaxScore());
        assignment.setUpdatedAt(LocalDateTime.now());
        
        // If course ID is provided and different, update course
        if (assignmentRequest.getCourseId() != null && 
            !assignmentRequest.getCourseId().equals(assignment.getCourse().getId())) {
            Course newCourse = courseRepository.findById(assignmentRequest.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
            
            // Check if user has permission to manage assignment in new course
            if (!hasPermissionToManageAssignment(currentUser, newCourse)) {
                throw new RuntimeException("No permission to create assignment in the target course");
            }
            
            assignment.setCourse(newCourse);
        }
        
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        logger.info("Assignment updated successfully, ID: {}", id);
        
        return convertToAssignmentResponse(updatedAssignment);
    }
    
    /**
     * Delete assignment
     * @param id assignment ID
     * @param username current user username
     * @throws RuntimeException when assignment not found or user has no permission
     */
    public void deleteAssignment(Long id, String username) {
        logger.info("Deleting assignment, ID: {}, user: {}", id, username);
        
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has permission to delete this assignment
        if (!hasPermissionToManageAssignment(currentUser, assignment.getCourse()) && 
            !assignment.getCreatedBy().getId().equals(currentUser.getId())) {
            logger.warn("User {} does not have permission to delete assignment {}", username, id);
            throw new RuntimeException("No permission to delete this assignment");
        }
        
        // Soft delete - set isActive to false
        assignment.setIsActive(false);
        assignment.setUpdatedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        
        logger.info("Assignment deleted successfully, ID: {}", id);
    }
    
    /**
     * Submit assignment
     * @param assignmentId assignment ID
     * @param submissionRequest submission request
     * @param username student username
     * @return submission response
     * @throws RuntimeException when assignment not found or submission deadline passed
     */
    public AssignmentSubmissionResponse submitAssignment(Long assignmentId, AssignmentSubmissionRequest submissionRequest, String username) {
        logger.info("Submitting assignment, assignment ID: {}, student: {}", assignmentId, username);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get student user information
        User student = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if assignment is still active
        if (!assignment.getIsActive()) {
            throw new RuntimeException("Assignment is no longer active");
        }
        
        // Check if submission deadline has passed
        if (assignment.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Assignment submission deadline has passed");
        }
        
        // Check if student already has a submission for this assignment
        Optional<AssignmentSubmission> existingSubmission = submissionRepository
            .findByAssignmentIdAndStudentId(assignmentId, student.getId());
        
        AssignmentSubmission submission;
        if (existingSubmission.isPresent()) {
            // Update existing submission
            submission = existingSubmission.get();
            submission.setContent(submissionRequest.getContent());
            submission.setSubmittedAt(LocalDateTime.now());
            logger.info("Updating existing submission for assignment {}, student {}", assignmentId, username);
        } else {
            // Create new submission
            submission = new AssignmentSubmission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setContent(submissionRequest.getContent());
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setCreatedAt(LocalDateTime.now());
            logger.info("Creating new submission for assignment {}, student {}", assignmentId, username);
        }
        
        submission.setUpdatedAt(LocalDateTime.now());
        
        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        logger.info("Assignment submission successful, submission ID: {}", savedSubmission.getId());
        
        return convertToSubmissionResponse(savedSubmission);
    }
    
    /**
     * Get assignment submissions
     * @param assignmentId assignment ID
     * @param pageable pagination information
     * @param username current user username
     * @return submission page
     * @throws RuntimeException when assignment not found or user has no permission
     */
    @Transactional(readOnly = true)
    public Page<AssignmentSubmissionResponse> getAssignmentSubmissions(Long assignmentId, Pageable pageable, String username) {
        logger.info("Getting assignment submissions, assignment ID: {}, user: {}", assignmentId, username);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<AssignmentSubmission> submissionPage;
        
        // Teachers and admins can view all submissions, students can only view their own
        if (hasPermissionToManageAssignment(currentUser, assignment.getCourse())) {
            submissionPage = submissionRepository.findByAssignmentId(assignmentId, pageable);
        } else {
            submissionPage = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, currentUser.getId(), pageable);
        }
        
        return submissionPage.map(this::convertToSubmissionResponse);
    }
    
    /**
     * Upload assignment files
     * @param assignmentId assignment ID
     * @param files files to upload
     * @param username current user username
     * @return file URL list
     * @throws RuntimeException when assignment not found or user has no permission
     */
    public List<String> uploadAssignmentFiles(Long assignmentId, MultipartFile[] files, String username) {
        logger.info("Uploading assignment files, assignment ID: {}, file count: {}, user: {}", assignmentId, files.length, username);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has permission to upload files for this assignment
        if (!hasPermissionToManageAssignment(currentUser, assignment.getCourse()) && 
            !assignment.getCreatedBy().getId().equals(currentUser.getId())) {
            logger.warn("User {} does not have permission to upload files for assignment {}", username, assignmentId);
            throw new RuntimeException("No permission to upload files for this assignment");
        }
        
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String fileUrl = fileStorageService.storeFile(file, "assignments/" + assignmentId);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                logger.error("Failed to upload file: {}", e.getMessage());
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename());
            }
        }
        
        logger.info("Assignment files uploaded successfully, assignment ID: {}, file count: {}", assignmentId, fileUrls.size());
        return fileUrls;
    }
    
    /**
     * Get assignment files
     * @param assignmentId assignment ID
     * @param username current user username
     * @return file URL list
     * @throws RuntimeException when assignment not found or user has no permission
     */
    @Transactional(readOnly = true)
    public List<String> getAssignmentFiles(Long assignmentId, String username) {
        logger.info("Getting assignment files, assignment ID: {}, user: {}", assignmentId, username);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        // Get current user information
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has permission to view files for this assignment
        if (!hasPermissionToViewAssignment(currentUser, assignment)) {
            logger.warn("User {} does not have permission to view files for assignment {}", username, assignmentId);
            throw new RuntimeException("No permission to view files for this assignment");
        }
        
        try {
            List<String> fileUrls = fileStorageService.listFiles("assignments/" + assignmentId);
            logger.info("Assignment files retrieved successfully, assignment ID: {}, file count: {}", assignmentId, fileUrls.size());
            return fileUrls;
        } catch (Exception e) {
            logger.error("Failed to retrieve assignment files: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve assignment files");
        }
    }
    
    /**
     * Check if user has permission to manage assignment (create, update, delete)
     * @param user user
     * @param course course
     * @return whether has permission
     */
    private boolean hasPermissionToManageAssignment(User user, Course course) {
        // Admin and teacher roles can manage assignments
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_TEACHER"));
    }
    
    /**
     * Check if user has permission to view assignment
     * @param user user
     * @param assignment assignment
     * @return whether has permission
     */
    private boolean hasPermissionToViewAssignment(User user, Assignment assignment) {
        // Admin and teacher can view all assignments
        if (user.getRoles().stream().anyMatch(role -> 
            role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_TEACHER"))) {
            return true;
        }
        
        // Students can view assignments from courses they are enrolled in
        // This would require checking course enrollment, simplified for now
        return true;
    }
    
    /**
     * Check if user is a student
     * @param user user
     * @return whether is student
     */
    private boolean isStudent(User user) {
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_STUDENT"));
    }
    
    /**
     * Convert Assignment entity to AssignmentResponse DTO
     * @param assignment assignment entity
     * @return assignment response DTO
     */
    private AssignmentResponse convertToAssignmentResponse(Assignment assignment) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(assignment.getId());
        response.setTitle(assignment.getTitle());
        response.setDescription(assignment.getDescription());
        response.setInstructions(assignment.getInstructions());
        response.setDueDate(assignment.getDueDate());
        response.setMaxScore(assignment.getMaxScore());
        response.setIsActive(assignment.getIsActive());
        response.setCourseId(assignment.getCourse().getId());
        response.setCourseName(assignment.getCourse().getName());
        response.setCreatedBy(assignment.getCreatedBy().getUsername());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        
        return response;
    }
    
    /**
     * Convert AssignmentSubmission entity to AssignmentSubmissionResponse DTO
     * @param submission submission entity
     * @return submission response DTO
     */
    private AssignmentSubmissionResponse convertToSubmissionResponse(AssignmentSubmission submission) {
        AssignmentSubmissionResponse response = new AssignmentSubmissionResponse();
        response.setId(submission.getId());
        response.setAssignmentId(submission.getAssignment().getId());
        response.setAssignmentTitle(submission.getAssignment().getTitle());
        response.setStudentId(submission.getStudent().getId());
        response.setStudentName(submission.getStudent().getUsername());
        response.setContent(submission.getContent());
        response.setScore(submission.getScore());
        response.setFeedback(submission.getFeedback());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        response.setCreatedAt(submission.getCreatedAt());
        response.setUpdatedAt(submission.getUpdatedAt());
        
        return response;
    }
}
