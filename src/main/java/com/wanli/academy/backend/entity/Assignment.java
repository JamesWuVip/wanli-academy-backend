package com.wanli.academy.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 作业实体类
 * 包含作业基本信息和与用户、提交记录的关联关系
 */
@Entity
@Table(name = "assignments")
public class Assignment {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 255, message = "作业标题长度不能超过255个字符")
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "创建者不能为空")
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "max_score")
    private Integer maxScore;
    
    @Column(name = "status", length = 20)
    private String status = "DRAFT"; // DRAFT, PUBLISHED, CLOSED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 与User实体的多对一关系
     * 一个作业只能有一个创建者（总部教师）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;
    
    /**
     * 与Submission实体的一对多关系
     * 一个作业可以有多个提交记录
     */
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Submission> submissions = new ArrayList<>();
    
    /**
     * 与AssignmentFile实体的一对多关系
     * 一个作业可以有多个附件文件
     */
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AssignmentFile> files = new ArrayList<>();
    
    // JPA生命周期回调方法
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 构造函数
    public Assignment() {}
    
    public Assignment(String title, String description, Long creatorId) {
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
    }
    
    // Getter和Setter方法
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
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
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public List<Submission> getSubmissions() {
        return submissions;
    }
    
    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }
    
    public List<AssignmentFile> getFiles() {
        return files;
    }
    
    public void setFiles(List<AssignmentFile> files) {
        this.files = files;
    }
    
    // 便利方法
    public void addSubmission(Submission submission) {
        submissions.add(submission);
        submission.setAssignment(this);
    }
    
    public void removeSubmission(Submission submission) {
        submissions.remove(submission);
        submission.setAssignment(null);
    }
    
    public void addFile(AssignmentFile file) {
        files.add(file);
        file.setAssignment(this);
    }
    
    public void removeFile(AssignmentFile file) {
        files.remove(file);
        file.setAssignment(null);
    }
    
    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", creatorId=" + creatorId +
                ", dueDate=" + dueDate +
                ", maxScore=" + maxScore +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}