package com.wanli.academy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 作业提交请求DTO
 * 用于学生提交作业的数据传输对象
 */
public class AssignmentSubmissionRequest {
    
    @NotNull(message = "作业ID不能为空")
    private Long assignmentId;
    
    @NotBlank(message = "提交内容不能为空")
    @Size(max = 5000, message = "提交内容长度不能超过5000个字符")
    private String content;
    
    @Size(max = 500, message = "文件路径长度不能超过500个字符")
    private String filePath;
    
    // 默认构造函数
    public AssignmentSubmissionRequest() {}
    
    // 全参构造函数
    public AssignmentSubmissionRequest(Long assignmentId, String content, String filePath) {
        this.assignmentId = assignmentId;
        this.content = content;
        this.filePath = filePath;
    }
    
    // Getter和Setter方法
    public Long getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
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
    
    @Override
    public String toString() {
        return "AssignmentSubmissionRequest{" +
                "assignmentId=" + assignmentId +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}