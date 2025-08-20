package com.wanli.academy.backend.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 作业请求DTO
 * 用于创建和更新作业的请求数据传输
 */
public class AssignmentRequest {
    
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 200, message = "作业标题长度不能超过200个字符")
    private String title;
    
    @Size(max = 2000, message = "作业描述长度不能超过2000个字符")
    private String description;
    
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime dueDate;
    
    @NotNull(message = "最大分数不能为空")
    private Integer maxScore;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    private Boolean isActive = true;
    
    // 构造函数
    public AssignmentRequest() {}
    
    public AssignmentRequest(String title, String description, LocalDateTime dueDate, Integer maxScore, Long courseId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxScore = maxScore;
        this.courseId = courseId;
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
    
    public Integer getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "AssignmentRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", maxScore=" + maxScore +
                ", courseId=" + courseId +
                ", isActive=" + isActive +
                '}';
    }
}