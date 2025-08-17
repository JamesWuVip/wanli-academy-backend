package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 作业文件响应DTO
 */
@Schema(description = "作业文件响应")
public class AssignmentFileResponse {
    
    @Schema(description = "文件ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID id;
    
    @Schema(description = "作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID assignmentId;
    
    @Schema(description = "作业标题", example = "数学作业第一章")
    private String assignmentTitle;
    
    @Schema(description = "文件名", example = "homework_template.pdf")
    private String fileName;
    
    @Schema(description = "文件路径", example = "/uploads/assignments/homework_template.pdf")
    private String filePath;
    
    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;
    
    @Schema(description = "文件类型", example = "application/pdf")
    private String fileType;
    
    @Schema(description = "文件分类", example = "TEMPLATE")
    private String fileCategory;
    
    @Schema(description = "上传者用户ID", example = "1")
    private Long uploaderId;
    
    @Schema(description = "上传者用户名", example = "张老师")
    private String uploaderUsername;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public AssignmentFileResponse() {}
    
    // 构造函数
    public AssignmentFileResponse(UUID id, UUID assignmentId, String assignmentTitle, String fileName, 
                                 String filePath, Long fileSize, String fileType, String fileCategory, 
                                 Long uploaderId, String uploaderUsername, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.fileCategory = fileCategory;
        this.uploaderId = uploaderId;
        this.uploaderUsername = uploaderUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(UUID assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public String getAssignmentTitle() {
        return assignmentTitle;
    }
    
    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getFileCategory() {
        return fileCategory;
    }
    
    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }
    
    public Long getUploaderId() {
        return uploaderId;
    }
    
    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }
    
    public String getUploaderUsername() {
        return uploaderUsername;
    }
    
    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderUsername = uploaderUsername;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 格式化文件大小显示
     * @return 格式化后的文件大小字符串
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "未知";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return "AssignmentFileResponse{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", assignmentTitle='" + assignmentTitle + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", fileCategory='" + fileCategory + '\'' +
                ", uploaderId=" + uploaderId +
                ", uploaderUsername='" + uploaderUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}