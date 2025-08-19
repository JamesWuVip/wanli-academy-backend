package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建作业请求DTO
 */
@Schema(description = "创建作业请求")
public class HomeworkCreateRequest {
    
    @Schema(description = "作业标题", example = "数学作业第一章", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "作业标题不能为空")
    @Size(min = 1, max = 200, message = "作业标题长度必须在1-200个字符之间")
    private String title;
    
    @Schema(description = "作业描述", example = "完成第一章的所有练习题，包括基础题和提高题")
    @Size(max = 1000, message = "作业描述长度不能超过1000个字符")
    private String description;
    
    // 默认构造函数
    public HomeworkCreateRequest() {}
    
    // 构造函数
    public HomeworkCreateRequest(String title, String description) {
        this.title = title;
        this.description = description;
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
    
    @Override
    public String toString() {
        return "HomeworkCreateRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}