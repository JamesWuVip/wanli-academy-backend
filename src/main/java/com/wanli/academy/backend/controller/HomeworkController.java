package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.HomeworkCreateRequest;
import com.wanli.academy.backend.dto.HomeworkResponse;
import com.wanli.academy.backend.dto.QuestionCreateRequest;
import com.wanli.academy.backend.dto.QuestionResponse;
import com.wanli.academy.backend.exception.ErrorResponse;
import com.wanli.academy.backend.service.HomeworkService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * Homework Controller
 * Handles homework related HTTP requests
 */
@Tag(name = "Homework Management", description = "Homework related API endpoints, including create homework, get homework list and add questions")
@RestController
@RequestMapping("/api/homeworks")
@PreAuthorize("hasRole('HQ_TEACHER')")
@SecurityRequirement(name = "Bearer Authentication")
public class HomeworkController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeworkController.class);
    
    @Autowired
    private HomeworkService homeworkService;
    
    /**
     * Create new homework
     * POST /api/homeworks
     * 
     * @param request Create homework request
     * @return Created homework information
     */
    @Operation(
        summary = "Create new homework",
        description = "Create a new homework, requires homework title and description. Only accessible by headquarters teacher role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Homework created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HomeworkResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions, headquarters teacher role required"
        )
    })
    @PostMapping
    public ResponseEntity<HomeworkResponse> createHomework(
            @Parameter(description = "Homework creation request information", required = true)
            @Valid @RequestBody HomeworkCreateRequest request) {
        logger.info("Received request to create homework: {}", request.getTitle());
        
        try {
            HomeworkResponse response = homeworkService.createHomework(request);
            logger.info("Successfully created homework with ID: {}", response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating homework: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get homework list created by current user
     * GET /api/homeworks
     * 
     * @return Homework list
     */
    @Operation(
        summary = "Get homework list",
        description = "Get all homework list created by current logged-in user. Only accessible by headquarters teacher role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved homework list",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HomeworkResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions, headquarters teacher role required"
        )
    })
    @GetMapping
    public ResponseEntity<List<HomeworkResponse>> getHomeworks() {
        logger.info("Received request to get homeworks for current user");
        
        try {
            List<HomeworkResponse> homeworks = homeworkService.getHomeworksByCreator();
            logger.info("Successfully retrieved {} homeworks", homeworks.size());
            return new ResponseEntity<>(homeworks, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving homeworks: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Add question to homework
     * POST /api/homeworks/{homeworkId}/questions
     * 
     * @param homeworkId Homework ID
     * @param request Question creation request
     * @return Created question information
     */
    @Operation(
        summary = "Add question to homework",
        description = "Add a new question to the specified homework. Question contains content, type, standard answer and other information. Only accessible by headquarters teacher role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Question added successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters or homework does not exist",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions, headquarters teacher role required"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Homework not found"
        )
    })
    @PostMapping("/{homeworkId}/questions")
    public ResponseEntity<QuestionResponse> addQuestionToHomework(
            @Parameter(description = "Unique identifier of the homework", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID homeworkId,
            @Parameter(description = "Question creation request information", required = true)
            @Valid @RequestBody QuestionCreateRequest request) {
        
        logger.info("Received request to add question to homework: {}", homeworkId);
        
        try {
            QuestionResponse response = homeworkService.addQuestionToHomework(homeworkId, request);
            logger.info("Successfully added question with ID: {} to homework: {}", 
                       response.getId(), homeworkId);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding question to homework {}: {}", homeworkId, e.getMessage(), e);
            throw e;
        }
    }
    

}
