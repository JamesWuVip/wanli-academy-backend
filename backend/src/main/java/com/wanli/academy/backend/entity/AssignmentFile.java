package com.wanli.academy.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 作业文件实体类
 * 包含作业相关文件的基本信息和关联关系
 */
@Entity
@Table(name = "assignment_files")
public class AssignmentFile {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotNull(message = "作业ID不能为空")
    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @NotBlank(message = "文件路径不能为空")
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Size(max = 100, message = "文件类型长度不能超过100个字符")
    @Column(name = "file_type", length = 100)
    private String fileType;
    
    @Size(max = 20, message = "文件分类长度不能超过20个字符")
    @Column(name = "file_category", length = 20)
    private String fileCategory = "ATTACHMENT"; // ATTACHMENT, TEMPLATE, REFERENCE
    
    @Size(max = 255, message = "原始文件名长度不能超过255个字符")
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;
    
    @Size(max = 100, message = "MIME类型长度不能超过100个字符")
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "uploaded_by")
    private Long uploadedBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 与Assignment实体的多对一关系
     * 一个文件只能属于一个作业
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;
    
    /**
     * 与User实体的多对一关系（上传者）
     * 一个文件只能由一个用户上传
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private User uploader;
    
    // JPA生命周期回调方法
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 构造函数
    public AssignmentFile() {}
    
    public AssignmentFile(UUID assignmentId, String fileName, String filePath, Long uploadedBy) {
        this.assignmentId = assignmentId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
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
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public Long getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
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
    
    public Assignment getAssignment() {
        return assignment;
    }
    
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
    
    public User getUploader() {
        return uploader;
    }
    
    public void setUploader(User uploader) {
        this.uploader = uploader;
    }
    
    @Override
    public String toString() {
        return "AssignmentFile{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", fileCategory='" + fileCategory + '\'' +
                ", uploadedBy=" + uploadedBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}