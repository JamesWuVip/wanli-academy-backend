package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 作业提交综合响应DTO
 * 包含作业信息和相关提交信息
 */
@Schema(description = "作业提交综合响应")
public class AssignmentSubmissionResponse {
    
    @Schema(description = "作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID assignmentId;
    
    @Schema(description = "作业标题", example = "数学作业第一章")
    private String assignmentTitle;
    
    @Schema(description = "作业描述", example = "完成第一章的所有练习题")
    private String assignmentDescription;
    
    @Schema(description = "创建者ID", example = "1")
    private Long creatorId;
    
    @Schema(description = "创建者用户名", example = "teacher1")
    private String creatorUsername;
    
    @Schema(description = "截止日期", example = "2024-01-20T23:59:59")
    private LocalDateTime dueDate;
    
    @Schema(description = "总分", example = "100")
    private Integer totalScore;
    
    @Schema(description = "作业状态", example = "PUBLISHED")
    private String assignmentStatus;
    
    @Schema(description = "作业创建时间", example = "2024-01-15T10:00:00")
    private LocalDateTime assignmentCreatedAt;
    
    @Schema(description = "提交列表")
    private List<SubmissionResponse> submissions;
    
    @Schema(description = "提交总数", example = "25")
    private Integer totalSubmissions;
    
    @Schema(description = "已批改数量", example = "20")
    private Integer gradedSubmissions;
    
    @Schema(description = "平均分", example = "85.5")
    private Double averageScore;
    
    // 默认构造函数
    public AssignmentSubmissionResponse() {}
    
    // 构造函数
    public AssignmentSubmissionResponse(UUID assignmentId, String assignmentTitle, String assignmentDescription,
                                      Long creatorId, String creatorUsername, LocalDateTime dueDate,
                                      Integer totalScore, String assignmentStatus, LocalDateTime assignmentCreatedAt,
                                      List<SubmissionResponse> submissions) {
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.assignmentDescription = assignmentDescription;
        this.creatorId = creatorId;
        this.creatorUsername = creatorUsername;
        this.dueDate = dueDate;
        this.totalScore = totalScore;
        this.assignmentStatus = assignmentStatus;
        this.assignmentCreatedAt = assignmentCreatedAt;
        this.submissions = submissions;
        
        // 计算统计信息
        if (submissions != null) {
            this.totalSubmissions = submissions.size();
            this.gradedSubmissions = (int) submissions.stream().filter(s -> s.getScore() != null).count();
            this.averageScore = submissions.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToInt(SubmissionResponse::getScore)
                    .average()
                    .orElse(0.0);
        } else {
            this.totalSubmissions = 0;
            this.gradedSubmissions = 0;
            this.averageScore = 0.0;
        }
    }
    
    // Getter和Setter方法
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
    
    public String getAssignmentDescription() {
        return assignmentDescription;
    }
    
    public void setAssignmentDescription(String assignmentDescription) {
        this.assignmentDescription = assignmentDescription;
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
    
    public String getAssignmentStatus() {
        return assignmentStatus;
    }
    
    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }
    
    public LocalDateTime getAssignmentCreatedAt() {
        return assignmentCreatedAt;
    }
    
    public void setAssignmentCreatedAt(LocalDateTime assignmentCreatedAt) {
        this.assignmentCreatedAt = assignmentCreatedAt;
    }
    
    public List<SubmissionResponse> getSubmissions() {
        return submissions;
    }
    
    public void setSubmissions(List<SubmissionResponse> submissions) {
        this.submissions = submissions;
        
        // 重新计算统计信息
        if (submissions != null) {
            this.totalSubmissions = submissions.size();
            this.gradedSubmissions = (int) submissions.stream().filter(s -> s.getScore() != null).count();
            this.averageScore = submissions.stream()
                    .filter(s -> s.getScore() != null)
                    .mapToInt(SubmissionResponse::getScore)
                    .average()
                    .orElse(0.0);
        } else {
            this.totalSubmissions = 0;
            this.gradedSubmissions = 0;
            this.averageScore = 0.0;
        }
    }
    
    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }
    
    public void setTotalSubmissions(Integer totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }
    
    public Integer getGradedSubmissions() {
        return gradedSubmissions;
    }
    
    public void setGradedSubmissions(Integer gradedSubmissions) {
        this.gradedSubmissions = gradedSubmissions;
    }
    
    public Double getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }
    
    @Override
    public String toString() {
        return "AssignmentSubmissionResponse{" +
                "assignmentId=" + assignmentId +
                ", assignmentTitle='" + assignmentTitle + '\'' +
                ", assignmentDescription='" + assignmentDescription + '\'' +
                ", creatorId=" + creatorId +
                ", creatorUsername='" + creatorUsername + '\'' +
                ", dueDate=" + dueDate +
                ", totalScore=" + totalScore +
                ", assignmentStatus='" + assignmentStatus + '\'' +
                ", assignmentCreatedAt=" + assignmentCreatedAt +
                ", totalSubmissions=" + totalSubmissions +
                ", gradedSubmissions=" + gradedSubmissions +
                ", averageScore=" + averageScore +
                '}';
    }
}