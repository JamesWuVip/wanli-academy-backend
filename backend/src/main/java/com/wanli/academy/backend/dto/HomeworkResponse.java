package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 作业响应DTO
 */
@Schema(description = "作业响应")
public class HomeworkResponse {
    
    @Schema(description = "作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "作业标题", example = "数学作业第一章")
    private String title;
    
    @Schema(description = "作业描述", example = "完成第一章的所有练习题，包括基础题和提高题")
    private String description;
    
    @Schema(description = "创建者用户ID", example = "1")
    private Long createdBy;
    
    @Schema(description = "创建者用户名", example = "张老师")
    private String createdByUsername;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "作业包含的题目列表")
    private List<QuestionResponse> questions;
    
    // 默认构造函数
    public HomeworkResponse() {}
    
    // 构造函数
    public HomeworkResponse(UUID id, String title, String description, Long createdBy, 
                           String createdByUsername, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.createdByUsername = createdByUsername;
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
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedByUsername() {
        return createdByUsername;
    }
    
    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
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
    
    public List<QuestionResponse> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
    
    @Override
    public String toString() {
        return "HomeworkResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdBy=" + createdBy +
                ", createdByUsername='" + createdByUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", questionsCount=" + (questions != null ? questions.size() : 0) +
                '}';
    }
}