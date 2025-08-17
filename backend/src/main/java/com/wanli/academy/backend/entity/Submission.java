package com.wanli.academy.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 作业提交实体类
 * 包含学生提交作业的基本信息和关联关系
 */
@Entity
@Table(name = "submissions")
public class Submission {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotNull(message = "作业ID不能为空")
    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;
    
    @NotNull(message = "学生ID不能为空")
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "score")
    private Integer score;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "status", length = 20)
    private String status = "SUBMITTED"; // SUBMITTED, GRADED, RETURNED
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
    
    @Column(name = "graded_by")
    private Long gradedBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 与Assignment实体的多对一关系
     * 一个提交记录只能属于一个作业
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;
    
    /**
     * 与User实体的多对一关系（学生）
     * 一个提交记录只能属于一个学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    /**
     * 与User实体的多对一关系（批改教师）
     * 一个提交记录只能由一个教师批改
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by", insertable = false, updatable = false)
    private User grader;
    
    // JPA生命周期回调方法
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 构造函数
    public Submission() {}
    
    public Submission(UUID assignmentId, Long studentId, String content) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.content = content;
    }
    
    // Getter和Setter方法
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(UUID assignmentId) {
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
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
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
    
    public Long getGradedBy() {
        return gradedBy;
    }
    
    public void setGradedBy(Long gradedBy) {
        this.gradedBy = gradedBy;
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
    
    public User getGrader() {
        return grader;
    }
    
    public void setGrader(User grader) {
        this.grader = grader;
    }
    
    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", assignmentId=" + assignmentId +
                ", studentId=" + studentId +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                ", score=" + score +
                ", feedback='" + feedback + '\'' +
                ", status='" + status + '\'' +
                ", submittedAt=" + submittedAt +
                ", gradedAt=" + gradedAt +
                ", gradedBy=" + gradedBy +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}