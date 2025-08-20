package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.AssignmentRequest;
import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.AssignmentSubmissionRequest;
import com.wanli.academy.backend.dto.AssignmentSubmissionResponse;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.AssignmentSubmission;
import com.wanli.academy.backend.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Assignment Controller
 * Handles assignment-related HTTP requests including creation, retrieval, update, deletion, and submission management
 */
@Tag(name = "Assignment Management", description = "Assignment management related API endpoints, including creation, retrieval, update, deletion, and submission management")
@RestController
@RequestMapping("/api/assignments")
@SecurityRequirement(name = "bearerAuth")
public class AssignmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    
    @Autowired
    private AssignmentService assignmentService;
    
    /**
     * Create Assignment
     * POST /api/assignments
     * 
     * @param assignmentRequest assignment creation request
     * @param bindingResult validation result
     * @param authentication authentication information
     * @return assignment creation response
     */
    @Operation(
        summary = "Create Assignment",
        description = "Create a new assignment. Only teachers and administrators can create assignments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Assignment created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> createAssignment(
            @Parameter(description = "Assignment creation request information", required = true)
            @Valid @RequestBody AssignmentRequest assignmentRequest,
            BindingResult bindingResult,
            Authentication authentication) {
        
        logger.info("Received assignment creation request, title: {}", assignmentRequest.getTitle());
        
        // Check request parameter validation results
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            String username = authentication.getName();
            AssignmentResponse assignmentResponse = assignmentService.createAssignment(assignmentRequest, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment created successfully");
            response.put("data", assignmentResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment created successfully, ID: {}", assignmentResponse.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Assignment creation failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get Assignment List
     * GET /api/assignments
     * 
     * @param page page number (starting from 0)
     * @param size page size
     * @param sortBy sort field
     * @param sortDir sort direction (asc/desc)
     * @param courseId course ID filter (optional)
     * @param status assignment status filter (optional)
     * @return assignment list
     */
    @Operation(
        summary = "Get Assignment List",
        description = "Get paginated assignment list with optional filtering by course and status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Assignment list retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        )
    })
    @GetMapping
    public ResponseEntity<?> getAssignments(
            @Parameter(description = "Page number (starting from 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Course ID filter") @RequestParam(required = false) Long courseId,
            @Parameter(description = "Assignment status filter") @RequestParam(required = false) String status,
            Authentication authentication) {
        
        logger.info("Received assignment list request, page: {}, size: {}", page, size);
        
        try {
            // Create pagination and sorting
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            String username = authentication.getName();
            Page<AssignmentResponse> assignmentPage = assignmentService.getAssignments(pageable, courseId, status, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment list retrieved successfully");
            response.put("data", assignmentPage.getContent());
            response.put("pagination", Map.of(
                "currentPage", assignmentPage.getNumber(),
                "totalPages", assignmentPage.getTotalPages(),
                "totalElements", assignmentPage.getTotalElements(),
                "size", assignmentPage.getSize(),
                "hasNext", assignmentPage.hasNext(),
                "hasPrevious", assignmentPage.hasPrevious()
            ));
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment list retrieved successfully, total: {}", assignmentPage.getTotalElements());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Failed to retrieve assignment list: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get Assignment Details
     * GET /api/assignments/{id}
     * 
     * @param id assignment ID
     * @param authentication authentication information
     * @return assignment details
     */
    @Operation(
        summary = "Get Assignment Details",
        description = "Get detailed information of a specific assignment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Assignment details retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignmentById(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            Authentication authentication) {
        
        logger.info("Received assignment details request, ID: {}", id);
        
        try {
            String username = authentication.getName();
            AssignmentResponse assignmentResponse = assignmentService.getAssignmentById(id, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment details retrieved successfully");
            response.put("data", assignmentResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment details retrieved successfully, ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Failed to retrieve assignment details: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Update Assignment
     * PUT /api/assignments/{id}
     * 
     * @param id assignment ID
     * @param assignmentRequest assignment update request
     * @param bindingResult validation result
     * @param authentication authentication information
     * @return assignment update response
     */
    @Operation(
        summary = "Update Assignment",
        description = "Update assignment information. Only the assignment creator, teachers, and administrators can update assignments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Assignment updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAssignment(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            @Parameter(description = "Assignment update request information", required = true)
            @Valid @RequestBody AssignmentRequest assignmentRequest,
            BindingResult bindingResult,
            Authentication authentication) {
        
        logger.info("Received assignment update request, ID: {}", id);
        
        // Check request parameter validation results
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            String username = authentication.getName();
            AssignmentResponse assignmentResponse = assignmentService.updateAssignment(id, assignmentRequest, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment updated successfully");
            response.put("data", assignmentResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment updated successfully, ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Assignment update failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : 
                              e.getMessage().contains("permission") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Delete Assignment
     * DELETE /api/assignments/{id}
     * 
     * @param id assignment ID
     * @param authentication authentication information
     * @return deletion response
     */
    @Operation(
        summary = "Delete Assignment",
        description = "Delete assignment. Only the assignment creator, teachers, and administrators can delete assignments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Assignment deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAssignment(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            Authentication authentication) {
        
        logger.info("Received assignment deletion request, ID: {}", id);
        
        try {
            String username = authentication.getName();
            assignmentService.deleteAssignment(id, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment deleted successfully");
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment deleted successfully, ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Assignment deletion failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : 
                              e.getMessage().contains("permission") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Submit Assignment
     * POST /api/assignments/{id}/submissions
     * 
     * @param id assignment ID
     * @param submissionRequest submission request
     * @param bindingResult validation result
     * @param authentication authentication information
     * @return submission response
     */
    @Operation(
        summary = "Submit Assignment",
        description = "Submit assignment solution. Only students can submit assignments."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Assignment submitted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentSubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @PostMapping("/{id}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitAssignment(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            @Parameter(description = "Assignment submission request information", required = true)
            @Valid @RequestBody AssignmentSubmissionRequest submissionRequest,
            BindingResult bindingResult,
            Authentication authentication) {
        
        logger.info("Received assignment submission request, assignment ID: {}", id);
        
        // Check request parameter validation results
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            String username = authentication.getName();
            AssignmentSubmissionResponse submissionResponse = assignmentService.submitAssignment(id, submissionRequest, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment submitted successfully");
            response.put("data", submissionResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment submitted successfully, submission ID: {}", submissionResponse.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("Assignment submission failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Get Assignment Submissions
     * GET /api/assignments/{id}/submissions
     * 
     * @param id assignment ID
     * @param page page number
     * @param size page size
     * @param authentication authentication information
     * @return submission list
     */
    @Operation(
        summary = "Get Assignment Submissions",
        description = "Get assignment submission list. Teachers and administrators can view all submissions, students can only view their own submissions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Submission list retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @GetMapping("/{id}/submissions")
    public ResponseEntity<?> getAssignmentSubmissions(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            @Parameter(description = "Page number (starting from 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        logger.info("Received assignment submissions request, assignment ID: {}", id);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
            String username = authentication.getName();
            
            Page<AssignmentSubmissionResponse> submissionPage = assignmentService.getAssignmentSubmissions(id, pageable, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Submission list retrieved successfully");
            response.put("data", submissionPage.getContent());
            response.put("pagination", Map.of(
                "currentPage", submissionPage.getNumber(),
                "totalPages", submissionPage.getTotalPages(),
                "totalElements", submissionPage.getTotalElements(),
                "size", submissionPage.getSize(),
                "hasNext", submissionPage.hasNext(),
                "hasPrevious", submissionPage.hasPrevious()
            ));
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Submission list retrieved successfully, total: {}", submissionPage.getTotalElements());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Failed to retrieve submission list: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Upload Assignment Files
     * POST /api/assignments/{id}/files
     * 
     * @param id assignment ID
     * @param files uploaded files
     * @param authentication authentication information
     * @return file upload response
     */
    @Operation(
        summary = "Upload Assignment Files",
        description = "Upload files for assignment. Only the assignment creator, teachers, and administrators can upload files."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Files uploaded successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @PostMapping("/{id}/files")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadAssignmentFiles(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            @Parameter(description = "Files to upload", required = true) @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        
        logger.info("Received assignment file upload request, assignment ID: {}, file count: {}", id, files.length);
        
        try {
            String username = authentication.getName();
            List<String> fileUrls = assignmentService.uploadAssignmentFiles(id, files, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            response.put("data", Map.of("fileUrls", fileUrls));
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment files uploaded successfully, assignment ID: {}, file count: {}", id, fileUrls.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Assignment file upload failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : 
                              e.getMessage().contains("permission") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Get Assignment Files
     * GET /api/assignments/{id}/files
     * 
     * @param id assignment ID
     * @param authentication authentication information
     * @return file list
     */
    @Operation(
        summary = "Get Assignment Files",
        description = "Get assignment file list"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "File list retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Assignment not found"
        )
    })
    @GetMapping("/{id}/files")
    public ResponseEntity<?> getAssignmentFiles(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id,
            Authentication authentication) {
        
        logger.info("Received assignment files request, assignment ID: {}", id);
        
        try {
            String username = authentication.getName();
            List<String> fileUrls = assignmentService.getAssignmentFiles(id, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File list retrieved successfully");
            response.put("data", Map.of("fileUrls", fileUrls));
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Assignment files retrieved successfully, assignment ID: {}, file count: {}", id, fileUrls.size());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Failed to retrieve assignment files: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    
    /**
     * Create validation error response
     * @param bindingResult validation result
     * @return error response Map
     */
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Request parameter validation failed");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        // Collect all validation errors
        Map<String, String> fieldErrors = bindingResult.getFieldErrors().stream()
            .collect(Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage(),
                (existing, replacement) -> existing // If there are duplicate fields, keep the first error message
            ));
        
        errorResponse.put("errors", fieldErrors);
        
        return errorResponse;
    }
    
    /**
     * Global exception handling
     * @param e exception
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("Unhandled exception in assignment controller: ", e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Internal server error");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
