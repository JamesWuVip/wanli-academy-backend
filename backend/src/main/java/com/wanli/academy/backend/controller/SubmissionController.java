package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.SubmissionResultDTO;
import com.wanli.academy.backend.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
 * 作业提交控制器
 * 处理作业提交相关的HTTP请求
 */
@Tag(name = "作业提交管理", description = "作业提交相关的API端点，包括提交作业、更新提交、批改作业等")
@RestController
@RequestMapping("/api/submissions")
@SecurityRequirement(name = "Bearer Authentication")
public class SubmissionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
    
    @Autowired
    private SubmissionService submissionService;
    
    /**
     * 提交作业请求DTO
     */
    public static class SubmitAssignmentRequest {
        @NotBlank(message = "提交内容不能为空")
        @Size(max = 10000, message = "提交内容长度不能超过10000个字符")
        private String content;
        
        @Size(max = 500, message = "文件路径长度不能超过500个字符")
        private String filePath;
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
    
    /**
     * 批改作业请求DTO
     */
    public static class GradeSubmissionRequest {
        @Min(value = 0, message = "分数不能小于0")
        @Max(value = 100, message = "分数不能大于100")
        private Integer score;
        
        @Size(max = 2000, message = "反馈内容长度不能超过2000个字符")
        private String feedback;
        
        public Integer getScore() {
            return score;
        }
        
        public void setScore(Integer score) {
            this.score = score;
        }
        
        public String getFeedback() {
            return feedback;
        }
        
        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }
    
    /**
     * 提交作业
     * POST /api/submissions/assignments/{assignmentId}
     * 
     * @param assignmentId 作业ID
     * @param request 提交请求
     * @return 提交响应
     */
    @Operation(
        summary = "提交作业",
        description = "学生提交指定作业的内容和文件。需要学生角色权限。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "作业提交成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效或作业已过期"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "作业不存在"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "作业已提交，不能重复提交"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足"
        )
    })
    @PostMapping("/assignments/{assignmentId}")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId) and @permissionService.isStudent()")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId,
            @Parameter(description = "提交作业请求信息", required = true)
            @Valid @RequestBody SubmitAssignmentRequest request) {
        
        logger.info("Received request to submit assignment: {}", assignmentId);
        
        try {
            SubmissionResponse response = submissionService.submitAssignment(
                assignmentId, request.getContent(), request.getFilePath());
            
            logger.info("Assignment submitted successfully: {}", response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error submitting assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 更新提交内容
     * PUT /api/submissions/{submissionId}
     * 
     * @param submissionId 提交ID
     * @param request 更新请求
     * @return 更新后的提交响应
     */
    @Operation(
        summary = "更新提交内容",
        description = "学生更新自己的作业提交内容。只能更新未批改的提交。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "提交更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效或提交已批改"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "提交不存在"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，只能修改自己的提交"
        )
    })
    @PutMapping("/{submissionId}")
    @PreAuthorize("@permissionService.canAccessSubmission(#submissionId)")
    public ResponseEntity<SubmissionResponse> updateSubmission(
            @Parameter(description = "提交的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID submissionId,
            @Parameter(description = "更新提交请求信息", required = true)
            @Valid @RequestBody SubmitAssignmentRequest request) {
        
        logger.info("Received request to update submission: {}", submissionId);
        
        try {
            SubmissionResponse response = submissionService.updateSubmission(
                submissionId, request.getContent(), request.getFilePath());
            
            logger.info("Submission updated successfully: {}", submissionId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating submission {}: {}", submissionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 批改作业
     * POST /api/submissions/{submissionId}/grade
     * 
     * @param submissionId 提交ID
     * @param request 批改请求
     * @return 批改后的提交响应
     */
    @Operation(
        summary = "批改作业",
        description = "教师批改学生提交的作业，给出分数和反馈。只有作业创建者可以批改。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "批改成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效或分数超出范围"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "提交不存在"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，只能批改自己创建的作业"
        )
    })
    @PostMapping("/{submissionId}/grade")
    @PreAuthorize("@permissionService.canGradeSubmission(#submissionId)")
    public ResponseEntity<SubmissionResponse> gradeSubmission(
            @Parameter(description = "提交的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID submissionId,
            @Parameter(description = "批改请求信息", required = true)
            @Valid @RequestBody GradeSubmissionRequest request) {
        
        logger.info("Received request to grade submission: {}", submissionId);
        
        try {
            SubmissionResponse response = submissionService.gradeSubmission(
                submissionId, request.getScore(), request.getFeedback());
            
            logger.info("Submission graded successfully: {}", submissionId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error grading submission {}: {}", submissionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取学生的提交记录
     * GET /api/submissions/my-submissions
     * 
     * @return 学生的提交记录列表
     */
    @Operation(
        summary = "获取我的提交记录",
        description = "学生获取自己的所有作业提交记录。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取提交记录",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足"
        )
    })
    @GetMapping("/my-submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions() {
        logger.info("Received request to get my submissions");
        
        try {
            List<SubmissionResponse> submissions = submissionService.getStudentSubmissions();
            logger.info("Successfully retrieved {} submissions", submissions.size());
            return new ResponseEntity<>(submissions, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving my submissions: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取待批改的提交列表
     * GET /api/submissions/pending-grade
     * 
     * @return 待批改的提交列表
     */
    @Operation(
        summary = "获取待批改的提交列表",
        description = "教师获取需要批改的作业提交列表。只显示自己创建的作业的提交。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取待批改列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，需要教师角色"
        )
    })
    @GetMapping("/pending-grade")
    @PreAuthorize("@permissionService.isTeacher()")
    public ResponseEntity<List<SubmissionResponse>> getPendingGradeSubmissions() {
        logger.info("Received request to get pending grade submissions");
        
        try {
            List<SubmissionResponse> submissions = submissionService.getPendingGradeSubmissions();
            logger.info("Successfully retrieved {} pending submissions", submissions.size());
            return new ResponseEntity<>(submissions, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving pending grade submissions: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 根据ID获取提交详情
     * GET /api/submissions/{submissionId}
     * 
     * @param submissionId 提交ID
     * @return 提交详情
     */
    @Operation(
        summary = "获取提交详情",
        description = "获取指定提交的详细信息。学生只能查看自己的提交，教师可以查看自己创建的作业的提交。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取提交详情",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "提交不存在"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足"
        )
    })
    @GetMapping("/{submissionId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('HQ_TEACHER')")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @Parameter(description = "提交的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID submissionId) {
        
        logger.info("Received request to get submission: {}", submissionId);
        
        try {
            SubmissionResponse response = submissionService.getSubmissionById(submissionId);
            logger.info("Successfully retrieved submission: {}", submissionId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving submission {}: {}", submissionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取作业提交结果详情（包含题目解析和视频讲解）
     * GET /api/submissions/{submissionId}/result
     * 
     * @param submissionId 提交ID
     * @return 提交结果详情
     */
    @Operation(
        summary = "获取作业提交结果详情",
        description = "获取作业提交的详细结果，包含题目解析和视频讲解。学生只能查看自己的提交结果，教师可以查看自己创建的作业的提交结果。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取提交结果详情",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResultDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "提交不存在"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，只能查看自己的提交或自己创建的作业的提交"
        )
    })
    @GetMapping("/{submissionId}/result")
    @PreAuthorize("hasRole('STUDENT') or hasRole('HQ_TEACHER') or hasRole('FRANCHISE_TEACHER')")
    public ResponseEntity<SubmissionResultDTO> getSubmissionResult(
            @Parameter(description = "提交的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID submissionId) {
        
        logger.info("Received request to get submission result: {}", submissionId);
        
        try {
            SubmissionResultDTO result = submissionService.getSubmissionResult(submissionId);
            logger.info("Successfully retrieved submission result: {}", submissionId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving submission result {}: {}", submissionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取作业统计信息
     * GET /api/submissions/assignments/{assignmentId}/statistics
     * 
     * @param assignmentId 作业ID
     * @return 作业统计信息
     */
    @Operation(
        summary = "获取作业统计信息",
        description = "获取指定作业的提交统计信息，包括提交数量、平均分等。只有教师可以访问。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取统计信息"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "作业不存在"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，需要教师角色"
        )
    })
    @GetMapping("/assignments/{assignmentId}/statistics")
    @PreAuthorize("hasRole('HQ_TEACHER')")
    public ResponseEntity<?> getAssignmentStatistics(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId) {
        
        logger.info("Received request to get statistics for assignment: {}", assignmentId);
        
        try {
            var statistics = submissionService.getAssignmentStatistics(assignmentId);
            logger.info("Successfully retrieved statistics for assignment: {}", assignmentId);
            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving statistics for assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }
    

}