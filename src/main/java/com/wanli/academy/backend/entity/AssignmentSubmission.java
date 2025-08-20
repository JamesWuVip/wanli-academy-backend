package com.wanli.academy.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 作业提交实体类
 */
@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    @Column(precision = 5, scale = 2)
    private Double score;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @Column(nullable = false, length = 20)
    private String status = "SUBMITTED";
    
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
    
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    // 构造函数
    public AssignmentSubmission() {
        this.submittedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public AssignmentSubmission(Long assignmentId, Long studentId, String content) {
        this();
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.content = content;
    }
    
    // JPA生命周期回调
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
        if (score != null) {
            this.gradedAt = LocalDateTime.now();
            this.status = "GRADED";
        }
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public Assignment getAssignment() {
        return assignment;
    }
    
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
    
    public User getStudent() {
        return student;
    }
    
    public void setStudent(User student) {
        this.student = student;
    }
}