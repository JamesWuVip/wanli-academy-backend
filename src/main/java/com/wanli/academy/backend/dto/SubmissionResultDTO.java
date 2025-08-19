package com.wanli.academy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 作业提交结果DTO
 * 用于返回作业提交的详细结果，包含题目解析和视频讲解
 */
@Schema(description = "作业提交结果")
public class SubmissionResultDTO {
    
    @Schema(description = "提交ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID submissionId;
    
    @Schema(description = "作业ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID assignmentId;
    
    @Schema(description = "作业标题", example = "数学作业第一章")
    private String assignmentTitle;
    
    @Schema(description = "学生ID", example = "1")
    private Long studentId;
    
    @Schema(description = "学生用户名", example = "student001")
    private String studentUsername;
    
    @Schema(description = "提交内容", example = "我的答案是...")
    private String content;
    
    @Schema(description = "提交文件路径", example = "/uploads/submission_123.pdf")
    private String filePath;
    
    @Schema(description = "提交状态", example = "GRADED")
    private String status;
    
    @Schema(description = "得分", example = "85")
    private Integer score;
    
    @Schema(description = "满分", example = "100")
    private Integer maxScore;
    
    @Schema(description = "教师反馈", example = "答题思路正确，但计算有小错误")
    private String feedback;
    
    @Schema(description = "教师评语", example = "答题思路正确，但计算有小错误")
    private String teacherFeedback;
    
    @Schema(description = "提交时间", example = "2024-01-15T10:30:00")
    private LocalDateTime submittedAt;
    
    @Schema(description = "批改时间", example = "2024-01-16T14:20:00")
    private LocalDateTime gradedAt;
    
    @Schema(description = "题目列表（包含解析和视频讲解）")
    private List<QuestionResponse> questions;
    
    // 默认构造函数
    public SubmissionResultDTO() {}
    
    // 完整构造函数
    public SubmissionResultDTO(UUID submissionId, UUID assignmentId, String assignmentTitle,
                              Long studentId, String studentUsername, String content, String filePath,
                              String status, Integer score, Integer maxScore, String feedback,
                              LocalDateTime submittedAt, LocalDateTime gradedAt, List<QuestionResponse> questions) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.studentUsername = studentUsername;
        this.content = content;
        this.filePath = filePath;
        this.status = status;
        this.score = score;
        this.maxScore = maxScore;
        this.feedback = feedback;
        this.teacherFeedback = feedback; // 同时设置teacherFeedback字段
        this.submittedAt = submittedAt;
        this.gradedAt = gradedAt;
        this.questions = questions;
    }
    
    // Getter和Setter方法
    public UUID getSubmissionId() {
        return submissionId;
    }
    
    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public Integer getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getTeacherFeedback() {
        return teacherFeedback;
    }
    
    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
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
    
    public List<QuestionResponse> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
    
    @Override
    public String toString() {
        return "SubmissionResultDTO{" +
                "submissionId=" + submissionId +
                ", assignmentId=" + assignmentId +
                ", assignmentTitle='" + assignmentTitle + '\'' +
                ", studentId=" + studentId +
                ", studentUsername='" + studentUsername + '\'' +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                ", status='" + status + '\'' +
                ", score=" + score +
                ", maxScore=" + maxScore +
                ", feedback='" + feedback + '\'' +
                ", submittedAt=" + submittedAt +
                ", gradedAt=" + gradedAt +
                ", questions=" + questions +
                '}';
    }
}