package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 作业提交响应DTO
 */
@Schema(description = "作业提交响应")
public class SubmissionResponse {
    
    @Schema(description = "提交ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;
    
    @Schema(description = "作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID assignmentId;
    
    @Schema(description = "作业标题", example = "数学作业第一章")
    private String assignmentTitle;
    
    @Schema(description = "学生用户ID", example = "2")
    private Long studentId;
    
    @Schema(description = "学生用户名", example = "张三")
    private String studentUsername;
    
    @Schema(description = "提交内容", example = "这是我的作业答案...")
    private String content;
    
    @Schema(description = "文件路径", example = "/uploads/submissions/homework1_zhangsan.pdf")
    private String filePath;
    
    @Schema(description = "得分", example = "85")
    private Integer score;
    
    @Schema(description = "教师反馈", example = "答案基本正确，但需要注意计算细节")
    private String feedback;
    
    @Schema(description = "提交状态", example = "SUBMITTED")
    private String status;
    
    @Schema(description = "提交时间", example = "2024-01-15T10:30:00")
    private LocalDateTime submittedAt;
    
    @Schema(description = "批改时间", example = "2024-01-16T14:20:00")
    private LocalDateTime gradedAt;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-16T14:20:00")
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public SubmissionResponse() {}
    
    // 构造函数
    public SubmissionResponse(UUID id, UUID assignmentId, String assignmentTitle, Long studentId, 
                             String studentUsername, String content, String filePath, Integer score, 
                             String feedback, String status, LocalDateTime submittedAt, 
                             LocalDateTime gradedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.content = content;
        this.filePath = filePath;
        this.score = score;
        this.feedback = feedback;
        this.status = status;
        this.submittedAt = submittedAt;
        this.gradedAt = gradedAt;
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
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentUsername() {
        return studentUsername;
    }
    
    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }
    
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getGradedAt() {
        return gradedAt;
    }
    
    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
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
    
    @Override
    public String toString() {
        return "SubmissionResponse{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", assignmentTitle='" + assignmentTitle + '\'' +
                ", studentId=" + studentId +
                ", studentUsername='" + studentUsername + '\'' +
                ", content='" + (content != null && content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
                ", filePath='" + filePath + '\'' +
                ", score=" + score +
                ", feedback='" + (feedback != null && feedback.length() > 50 ? feedback.substring(0, 50) + "..." : feedback) + '\'' +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", gradedAt=" + gradedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}