package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.AssignmentCreateRequest;
import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.StudentAssignmentResponse;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.AssignmentFileResponse;
import com.wanli.academy.backend.exception.ErrorResponse;
import com.wanli.academy.backend.service.AssignmentService;
import com.wanli.academy.backend.service.AssignmentServiceQuery;
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
 * 作业控制器
 * 处理作业相关的HTTP请求
 */
@Tag(name = "作业管理", description = "作业相关的API端点，包括创建作业、获取作业列表、更新作业和删除作业")
@RestController
@RequestMapping("/api/assignments")

@SecurityRequirement(name = "Bearer Authentication")
public class AssignmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private AssignmentServiceQuery assignmentServiceQuery;

    /**
     * 创建新作业
     * POST /api/assignments
     * 
     * @param request 创建作业请求
     * @return 创建的作业信息
     */
    @Operation(
        summary = "创建新作业",
        description = "创建一个新的作业，需要提供作业标题、描述、截止日期等信息。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "作业创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，需要总部教师角色"
        )
    })
    @PostMapping
    @PreAuthorize("@permissionService.isTeacher()")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @Parameter(description = "作业创建请求信息", required = true)
            @Valid @RequestBody AssignmentCreateRequest request) {
        logger.info("Received request to create assignment: {}", request.getTitle());
        
        try {
            AssignmentResponse response = assignmentService.createAssignment(request);
            logger.info("Successfully created assignment with ID: {}", response.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating assignment: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取当前用户创建的作业列表
     * GET /api/assignments
     * 
     * @return 作业列表
     */
    @Operation(
        summary = "获取作业列表",
        description = "获取当前登录用户创建的所有作业列表。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取作业列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，需要总部教师角色"
        )
    })
    @GetMapping
    @PreAuthorize("@permissionService.isTeacher()")
    public ResponseEntity<List<AssignmentResponse>> getAssignments() {
        logger.info("Received request to get assignments for current user");
        
        try {
            List<AssignmentResponse> assignments = assignmentServiceQuery.getAssignmentsByCreator();
            logger.info("Successfully retrieved {} assignments", assignments.size());
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving assignments: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据ID获取作业详情
     * GET /api/assignments/{assignmentId}
     * 
     * @param assignmentId 作业ID
     * @return 作业详情
     */
    @Operation(
        summary = "获取作业详情",
        description = "根据作业ID获取作业的详细信息。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取作业详情",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
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
            description = "权限不足，需要总部教师角色"
        )
    })
    @GetMapping("/{assignmentId}")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId)")
    public ResponseEntity<AssignmentResponse> getAssignmentById(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId) {
        logger.info("Received request to get assignment: {}", assignmentId);
        
        try {
            AssignmentResponse response = assignmentServiceQuery.getAssignmentById(assignmentId);
            logger.info("Successfully retrieved assignment: {}", assignmentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 更新作业信息
     * PUT /api/assignments/{assignmentId}
     * 
     * @param assignmentId 作业ID
     * @param request 更新作业请求
     * @return 更新后的作业信息
     */
    @Operation(
        summary = "更新作业信息",
        description = "更新指定作业的信息，包括标题、描述、截止日期等。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "作业更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效"
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
            description = "权限不足，需要总部教师角色"
        )
    })
    @PutMapping("/{assignmentId}")
    @PreAuthorize("@permissionService.canModifyAssignment(#assignmentId)")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId,
            @Parameter(description = "作业更新请求信息", required = true)
            @Valid @RequestBody AssignmentCreateRequest request) {
        logger.info("Received request to update assignment: {}", assignmentId);
        
        try {
            AssignmentResponse response = assignmentService.updateAssignment(assignmentId, request);
            logger.info("Successfully updated assignment: {}", assignmentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除作业
     * DELETE /api/assignments/{assignmentId}
     * 
     * @param assignmentId 作业ID
     * @return 删除结果
     */
    @Operation(
        summary = "删除作业",
        description = "删除指定的作业及其相关的所有数据。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "作业删除成功"
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
            description = "权限不足，需要总部教师角色"
        )
    })
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("@permissionService.canModifyAssignment(#assignmentId)")
    public ResponseEntity<Void> deleteAssignment(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId) {
        logger.info("Received request to delete assignment: {}", assignmentId);
        
        try {
            assignmentService.deleteAssignment(assignmentId);
            logger.info("Successfully deleted assignment: {}", assignmentId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取作业的提交列表
     * GET /api/assignments/{assignmentId}/submissions
     * 
     * @param assignmentId 作业ID
     * @return 提交列表
     */
    @Operation(
        summary = "获取作业提交列表",
        description = "获取指定作业的所有提交记录。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取提交列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubmissionResponse.class)
            )
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
            description = "权限不足，需要总部教师角色"
        )
    })
    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId)")
    public ResponseEntity<List<SubmissionResponse>> getAssignmentSubmissions(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId) {
        logger.info("Received request to get submissions for assignment: {}", assignmentId);
        
        try {
            List<SubmissionResponse> submissions = assignmentServiceQuery.getSubmissionsByAssignment(assignmentId);
            logger.info("Successfully retrieved {} submissions for assignment: {}", submissions.size(), assignmentId);
            return new ResponseEntity<>(submissions, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving submissions for assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取作业的文件列表
     * GET /api/assignments/{assignmentId}/files
     * 
     * @param assignmentId 作业ID
     * @return 文件列表
     */
    @Operation(
        summary = "获取作业文件列表",
        description = "获取指定作业的所有相关文件。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取文件列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentFileResponse.class)
            )
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
            description = "权限不足，需要总部教师角色"
        )
    })
    @GetMapping("/{assignmentId}/files")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId)")
    public ResponseEntity<List<AssignmentFileResponse>> getAssignmentFiles(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID assignmentId) {
        logger.info("Received request to get files for assignment: {}", assignmentId);
        
        try {
            List<AssignmentFileResponse> files = assignmentServiceQuery.getFilesByAssignment(assignmentId);
            logger.info("Successfully retrieved {} files for assignment: {}", files.size(), assignmentId);
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving files for assignment {}: {}", assignmentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取即将到期的作业列表
     * GET /api/assignments/due-soon
     * 
     * @param hours 小时数，默认为24小时
     * @return 即将到期的作业列表
     */
    @Operation(
        summary = "获取即将到期的作业",
        description = "获取在指定小时数内即将到期的作业列表。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取即将到期的作业列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，需要总部教师角色"
        )
    })
    @GetMapping("/due-soon")
    @PreAuthorize("@permissionService.isTeacher()")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsDueSoon(
            @Parameter(description = "小时数，默认为24小时", example = "24")
            @RequestParam(defaultValue = "24") int hours) {
        logger.info("Received request to get assignments due within {} hours", hours);
        
        try {
            List<AssignmentResponse> assignments = assignmentServiceQuery.getAssignmentsDueSoon(hours);
            logger.info("Successfully retrieved {} assignments due within {} hours", assignments.size(), hours);
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving assignments due soon: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取已发布的作业列表（学生专用）
     * GET /api/assignments/published
     * 
     * @return 已发布的作业列表
     */
    @Operation(
        summary = "获取已发布的作业列表",
        description = "学生获取所有已发布的作业列表，可以进行提交。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取已发布作业列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        )
    })
    @GetMapping("/published")
    @PreAuthorize("@permissionService.isStudent()")
    public ResponseEntity<List<AssignmentResponse>> getPublishedAssignments() {
        logger.info("Received request to get published assignments for student");
        
        try {
            List<AssignmentResponse> assignments = assignmentServiceQuery.getPublishedAssignments();
            logger.info("Successfully retrieved {} published assignments", assignments.size());
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving published assignments: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取学生的作业列表（包含提交状态）
     * GET /api/assignments/my-assignments
     * 
     * @return 包含学生提交状态的作业列表
     */
    @Operation(
        summary = "获取学生的作业列表",
        description = "学生获取所有已发布的作业列表，包含自己的提交状态和提交ID。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取学生作业列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StudentAssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        )
    })
    @GetMapping("/my-assignments")
    @PreAuthorize("@permissionService.isStudent()")
    public ResponseEntity<List<StudentAssignmentResponse>> getMyAssignments() {
        logger.info("Received request to get assignments with submission status for student");
        
        try {
            List<StudentAssignmentResponse> assignments = assignmentServiceQuery.getStudentAssignments();
            logger.info("Successfully retrieved {} assignments with submission status", assignments.size());
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving student assignments: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 根据状态获取作业列表
     * GET /api/assignments/status/{status}
     * 
     * @param status 作业状态
     * @return 指定状态的作业列表
     */
    @Operation(
        summary = "根据状态获取作业列表",
        description = "根据指定状态获取作业列表。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功获取作业列表",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AssignmentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权访问"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，需要总部教师角色"
        )
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("@permissionService.isTeacher()")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByStatus(
            @Parameter(description = "作业状态", required = true, example = "PUBLISHED")
            @PathVariable String status) {
        logger.info("Received request to get assignments by status: {}", status);
        
        try {
            List<AssignmentResponse> assignments = assignmentServiceQuery.getAssignmentsByStatus(status);
            logger.info("Successfully retrieved {} assignments with status: {}", assignments.size(), status);
            return new ResponseEntity<>(assignments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving assignments by status {}: {}", status, e.getMessage(), e);
            throw e;
        }
    }


}