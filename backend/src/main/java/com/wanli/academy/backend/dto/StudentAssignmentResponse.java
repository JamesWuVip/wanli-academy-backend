package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学生作业响应DTO（包含提交状态）
 */
@Schema(description = "学生作业响应")
public class StudentAssignmentResponse {
    
    @Schema(description = "作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "作业标题", example = "数学作业第一章")
    private String title;
    
    @Schema(description = "作业描述", example = "完成第一章的所有练习题，包括基础题和提高题")
    private String description;
    
    @Schema(description = "创建者用户ID", example = "1")
    private Long creatorId;
    
    @Schema(description = "创建者用户名", example = "张老师")
    private String creatorUsername;
    
    @Schema(description = "作业截止日期", example = "2024-12-31T23:59:59")
    private LocalDateTime dueDate;
    
    @Schema(description = "作业总分", example = "100")
    private Integer totalScore;
    
    @Schema(description = "作业状态", example = "PUBLISHED")
    private String status;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    // 学生特定的字段
    @Schema(description = "学生提交ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID submissionId;
    
    @Schema(description = "学生提交状态", example = "GRADED")
    private String submissionStatus;
    
    @Schema(description = "学生得分", example = "85")
    private Integer score;
    
    @Schema(description = "提交时间", example = "2024-01-20T14:30:00")
    private LocalDateTime submittedAt;
    
    @Schema(description = "批阅时间", example = "2024-01-21T10:00:00")
    private LocalDateTime gradedAt;
    
    // 默认构造函数
    public StudentAssignmentResponse() {}
    
    // Getter和Setter方法
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getCreatorUsername() {
        return creatorUsername;
    }
    
    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public Integer getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public UUID getSubmissionId() {
        return submissionId;
    }
    
    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }
    
    public String getSubmissionStatus() {
        return submissionStatus;
    }
    
    public void setSubmissionStatus(String submissionStatus) {
        this.submissionStatus = submissionStatus;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
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
    
    @Override
    public String toString() {
        return "StudentAssignmentResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creatorId=" + creatorId +
                ", creatorUsername='" + creatorUsername + '\'' +
                ", dueDate=" + dueDate +
                ", totalScore=" + totalScore +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", submissionId=" + submissionId +
                ", submissionStatus='" + submissionStatus + '\'' +
                ", score=" + score +
                ", submittedAt=" + submittedAt +
                ", gradedAt=" + gradedAt +
                '}';
    }
}