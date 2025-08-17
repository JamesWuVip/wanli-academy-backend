package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.FileResponse;
import com.wanli.academy.backend.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * 文件管理控制器
 * 处理文件上传、下载、删除等操作
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上传成功"),
            @ApiResponse(responseCode = "400", description = "文件格式不支持或文件过大"),
            @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<FileResponse> uploadFile(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") @NotNull MultipartFile file,
            @Parameter(description = "关联的作业ID")
            @RequestParam(value = "assignmentId", required = false) UUID assignmentId,
            @Parameter(description = "文件类别 (ASSIGNMENT_FILE, SUBMISSION_FILE, TEMP_FILE)", required = true)
            @RequestParam("fileCategory") @NotNull String fileCategory) {
        
        try {
            logger.info("Uploading file: {} for assignment: {} with category: {}", 
                    file.getOriginalFilename(), assignmentId, fileCategory);
            
            FileResponse response = fileService.uploadFile(file, assignmentId, fileCategory);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid file upload request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            logger.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    @PreAuthorize("@permissionService.canAccessFile(#fileId)")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "下载成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable @NotNull UUID fileId) {
        
        try {
            logger.info("Downloading file: {}", fileId);
            
            Resource resource = fileService.downloadFile(fileId);
            
            // 获取文件信息用于设置响应头
            FileResponse fileInfo = fileService.getFileInfo(fileId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileInfo.getOriginalFileName() + "\"")
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            logger.error("File not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Access denied for file download: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            logger.error("File download failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("@permissionService.canDeleteFile(#fileId)")
    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "403", description = "无权限删除")
    })
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "文件ID", required = true)
            @PathVariable @NotNull UUID fileId) {
        
        try {
            logger.info("Deleting file: {}", fileId);
            
            fileService.deleteFile(fileId);
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            logger.error("File not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Access denied for file deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            logger.error("File deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取文件信息
     */
    @GetMapping("/{fileId}/info")
    @PreAuthorize("@permissionService.canAccessFile(#fileId)")
    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "文件不存在"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    public ResponseEntity<FileResponse> getFileInfo(
            @Parameter(description = "文件ID", required = true)
            @PathVariable @NotNull UUID fileId) {
        
        try {
            logger.info("Getting file info: {}", fileId);
            
            FileResponse response = fileService.getFileInfo(fileId);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("File not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Access denied for file info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            logger.error("Get file info failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取作业相关文件列表
     */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId)")
    @Operation(summary = "获取作业文件列表", description = "获取指定作业的所有相关文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "作业不存在"),
            @ApiResponse(responseCode = "403", description = "无权限访问")
    })
    public ResponseEntity<List<FileResponse>> getAssignmentFiles(
            @Parameter(description = "作业ID", required = true)
            @PathVariable @NotNull UUID assignmentId,
            @Parameter(description = "文件类别过滤 (可选)")
            @RequestParam(value = "fileCategory", required = false) String fileCategory) {
        
        try {
            logger.info("Getting assignment files: {} with category: {}", assignmentId, fileCategory);
            
            List<FileResponse> files = fileService.getAssignmentFiles(assignmentId);
            return ResponseEntity.ok(files);
            
        } catch (IllegalArgumentException e) {
            logger.error("Assignment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            logger.error("Access denied for assignment files: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            logger.error("Get assignment files failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取用户上传的文件列表
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户文件列表", description = "获取当前用户上传的所有文件")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<List<FileResponse>> getUserFiles(
            @Parameter(description = "文件类别过滤 (可选)")
            @RequestParam(value = "fileCategory", required = false) String fileCategory) {
        
        try {
            logger.info("Getting user files with category: {}", fileCategory);
            
            List<FileResponse> files = fileService.getUserFiles();
            return ResponseEntity.ok(files);
            
        } catch (RuntimeException e) {
            logger.error("Get user files failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 清理过期临时文件
     */
    @PostMapping("/cleanup")
    @PreAuthorize("@permissionService.isAdmin()")
    @Operation(summary = "清理过期文件", description = "清理过期的临时文件（管理员功能）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "清理成功"),
            @ApiResponse(responseCode = "403", description = "无权限执行此操作")
    })
    public ResponseEntity<String> cleanupExpiredFiles() {
        
        try {
            logger.info("Starting cleanup of expired files");
            
            fileService.cleanupExpiredTempFiles();
            String message = "过期文件清理操作已完成";
            
            return ResponseEntity.ok(message);
            
        } catch (AccessDeniedException e) {
            logger.error("Access denied for file cleanup: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权限执行此操作");
        } catch (RuntimeException e) {
            logger.error("File cleanup failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("清理操作失败: " + e.getMessage());
        }
    }
}