package com.wanli.academy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 作业请求DTO
 * 用于创建和更新作业的数据传输对象
 */
public class AssignmentRequest {
    
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 200, message = "作业标题长度不能超过200个字符")
    private String title;
    
    @Size(max = 2000, message = "作业描述长度不能超过2000个字符")
    private String description;
    
    @NotNull(message = "截止日期不能为空")
    private LocalDateTime dueDate;
    
    @NotNull(message = "总分不能为空")
    @Positive(message = "总分必须大于0")
    private Integer totalPoints;
    
    @NotBlank(message = "作业状态不能为空")
    private String status;
    
    // 默认构造函数
    public AssignmentRequest() {}
    
    // 全参构造函数
    public AssignmentRequest(String title, String description, LocalDateTime dueDate, Integer totalPoints, String status) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.totalPoints = totalPoints;
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
    
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "AssignmentRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", dueDate=" + dueDate +
                ", totalPoints=" + totalPoints +
                ", status='" + status + '\'' +
                '}';
    }
}