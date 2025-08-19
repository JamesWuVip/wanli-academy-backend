package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDateTime;

/**
 * 创建作业请求DTO
 */
@Schema(description = "创建作业请求")
public class AssignmentCreateRequest {
    
    @Schema(description = "作业标题", example = "数学作业第一章", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "作业标题不能为空")
    @Size(min = 1, max = 200, message = "作业标题长度必须在1-200个字符之间")
    private String title;
    
    @Schema(description = "作业描述", example = "完成第一章的所有练习题，包括基础题和提高题")
    @Size(max = 2000, message = "作业描述长度不能超过2000个字符")
    private String description;
    
    @Schema(description = "作业截止日期", example = "2024-12-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "截止日期不能为空")
    private LocalDateTime dueDate;
    
    @Schema(description = "作业总分", example = "100")
    @Min(value = 0, message = "总分不能小于0")
    @Max(value = 1000, message = "总分不能超过1000")
    private Integer totalScore;
    
    @Schema(description = "作业状态", example = "DRAFT", allowableValues = {"DRAFT", "PUBLISHED", "CLOSED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "作业状态不能为空")
    private String status;
    
    // 默认构造函数
    public AssignmentCreateRequest() {}
    
    // 构造函数
    public AssignmentCreateRequest(String title, String description, LocalDateTime dueDate, Integer totalScore, String status) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.totalScore = totalScore;
        this.status = status;
    }
    
    // Getter和Setter方法
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
    
    @Override
    public String toString() {
        return "AssignmentCreateRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", totalScore=" + totalScore +
                ", status='" + status + '\'' +
                '}';
    }
}