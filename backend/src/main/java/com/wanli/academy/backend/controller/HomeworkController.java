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
 * 作业控制器
 * 处理作业相关的HTTP请求
 */
@Tag(name = "作业管理", description = "作业相关的API端点，包括创建作业、获取作业列表和添加题目")
@RestController
@RequestMapping("/api/homeworks")
@PreAuthorize("hasRole('HQ_TEACHER')")
@SecurityRequirement(name = "Bearer Authentication")
public class HomeworkController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeworkController.class);
    
    @Autowired
    private HomeworkService homeworkService;
    
    /**
     * 创建新作业
     * POST /api/homeworks
     * 
     * @param request 创建作业请求
     * @return 创建的作业信息
     */
    @Operation(
        summary = "创建新作业",
        description = "创建一个新的作业，需要提供作业标题和描述。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "作业创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HomeworkResponse.class)
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
    public ResponseEntity<HomeworkResponse> createHomework(
            @Parameter(description = "作业创建请求信息", required = true)
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
     * 获取当前用户创建的作业列表
     * GET /api/homeworks
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
                schema = @Schema(implementation = HomeworkResponse.class)
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
     * 向作业添加题目
     * POST /api/homeworks/{homeworkId}/questions
     * 
     * @param homeworkId 作业ID
     * @param request 题目创建请求
     * @return 创建的题目信息
     */
    @Operation(
        summary = "向作业添加题目",
        description = "向指定的作业中添加一个新题目。题目包含内容、类型、标准答案等信息。只有总部教师角色可以访问此接口。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "题目添加成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数无效或作业不存在",
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "作业不存在"
        )
    })
    @PostMapping("/{homeworkId}/questions")
    public ResponseEntity<QuestionResponse> addQuestionToHomework(
            @Parameter(description = "作业的唯一标识符", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID homeworkId,
            @Parameter(description = "题目创建请求信息", required = true)
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