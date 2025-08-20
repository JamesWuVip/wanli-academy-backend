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
 * File Management Controller
 * Handles file upload, download, delete and other operations
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "File Management", description = "File upload, download, delete and other operations")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileService fileService;
    
    /**
     * Upload file
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload file", description = "Upload file to server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload successful"),
            @ApiResponse(responseCode = "400", description = "Unsupported file format or file too large"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<FileResponse> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") @NotNull MultipartFile file,
            @Parameter(description = "Associated assignment ID")
            @RequestParam(value = "assignmentId", required = false) UUID assignmentId,
            @Parameter(description = "File category (ASSIGNMENT_FILE, SUBMISSION_FILE, TEMP_FILE)", required = true)
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
     * Download file
     */
    @GetMapping("/download/{fileId}")
    @PreAuthorize("@permissionService.canAccessFile(#fileId)")
    @Operation(summary = "Download file", description = "Download file by file ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Download successful"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "403", description = "No permission to access")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File ID", required = true)
            @PathVariable @NotNull UUID fileId) {
        
        try {
            logger.info("Downloading file: {}", fileId);
            
            Resource resource = fileService.downloadFile(fileId);
            
            // Get file information for setting response headers
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
     * Delete file
     */
    @DeleteMapping("/{fileId}")
    @PreAuthorize("@permissionService.canDeleteFile(#fileId)")
    @Operation(summary = "Delete file", description = "Delete file by file ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete successful"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "403", description = "No permission to delete")
    })
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "File ID", required = true)
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
     * Get file information
     */
    @GetMapping("/{fileId}/info")
    @PreAuthorize("@permissionService.canAccessFile(#fileId)")
    @Operation(summary = "Get file information", description = "Get detailed file information by file ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get successful"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "403", description = "No permission to access")
    })
    public ResponseEntity<FileResponse> getFileInfo(
            @Parameter(description = "File ID", required = true)
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
     * Get assignment related file list
     */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("@permissionService.canAccessAssignment(#assignmentId)")
    @Operation(summary = "Get assignment file list", description = "Get all related files for specified assignment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get successful"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "403", description = "No permission to access")
    })
    public ResponseEntity<List<FileResponse>> getAssignmentFiles(
            @Parameter(description = "Assignment ID", required = true)
            @PathVariable @NotNull UUID assignmentId,
            @Parameter(description = "File category filter (optional)")
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
     * Get user uploaded file list
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user file list", description = "Get all files uploaded by current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<FileResponse>> getUserFiles(
            @Parameter(description = "File category filter (optional)")
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
     * Clean up expired temporary files
     */
    @PostMapping("/cleanup")
    @PreAuthorize("@permissionService.isAdmin()")
    @Operation(summary = "Clean up expired files", description = "Clean up expired temporary files (admin function)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleanup successful"),
            @ApiResponse(responseCode = "403", description = "No permission to perform this operation")
    })
    public ResponseEntity<String> cleanupExpiredFiles() {
        
        try {
            logger.info("Starting cleanup of expired files");
            
            fileService.cleanupExpiredTempFiles();
            String message = "Expired file cleanup operation completed";
            
            return ResponseEntity.ok(message);
            
        } catch (AccessDeniedException e) {
            logger.error("Access denied for file cleanup: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No permission to perform this operation");
        } catch (RuntimeException e) {
            logger.error("File cleanup failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Cleanup operation failed: " + e.getMessage());
        }
    }
}