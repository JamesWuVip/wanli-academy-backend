package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 题目响应DTO
 */
@Schema(description = "题目响应")
public class QuestionResponse {
    
    @Schema(description = "题目ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID id;
    
    @Schema(description = "题目内容", example = "计算 2 + 3 = ?")
    private String content;
    
    @Schema(description = "题目类型", example = "选择题")
    private String questionType;
    
    @Schema(description = "标准答案", example = "5")
    private String standardAnswer;
    
    @Schema(description = "题目在作业中的顺序", example = "1")
    private Integer orderIndex;
    
    @Schema(description = "所属作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID homeworkId;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public QuestionResponse() {}
    
    // 构造函数
    public QuestionResponse(UUID id, String content, String questionType, String standardAnswer, 
                           Integer orderIndex, UUID homeworkId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.questionType = questionType;
        this.standardAnswer = standardAnswer;
        this.orderIndex = orderIndex;
        this.homeworkId = homeworkId;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public String getStandardAnswer() {
        return standardAnswer;
    }
    
    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }
    
    public Integer getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
    
    public UUID getHomeworkId() {
        return homeworkId;
    }
    
    public void setHomeworkId(UUID homeworkId) {
        this.homeworkId = homeworkId;
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
        return "QuestionResponse{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", questionType='" + questionType + '\'' +
                ", standardAnswer='" + standardAnswer + '\'' +
                ", orderIndex=" + orderIndex +
                ", homeworkId=" + homeworkId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}