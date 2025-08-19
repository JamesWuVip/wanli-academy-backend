package com.wanli.academy.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 题目实体类
 * 包含题目基本信息和与作业的关联关系
 */
@Entity
@Table(name = "questions")
public class Question {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @NotNull(message = "作业ID不能为空")
    @Column(name = "homework_id", nullable = false)
    private UUID homeworkId;
    
    @Size(max = 50, message = "题型长度不能超过50个字符")
    @Column(name = "question_type", length = 50)
    private String questionType;
    
    /**
     * 题目内容，使用JSON格式存储
     * 支持富文本、图片等复杂内容
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content")
    private Map<String, Object> content;
    
    /**
     * 标准答案，使用JSON格式存储
     * 支持复杂答案结构
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "standard_answer")
    private Map<String, Object> standardAnswer;
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 题目解析，用于提供详细的解题思路和方法
     */
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
    
    /**
     * 视频讲解链接，用于提供视频形式的题目讲解
     */
    @Size(max = 500, message = "视频链接长度不能超过500个字符")
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    /**
     * 与Homework实体的多对一关系
     * 一个题目只能属于一个作业
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", insertable = false, updatable = false)
    private Homework homework;
    
    // 构造函数
    public Question() {}
    
    public Question(UUID homeworkId, String questionType, Map<String, Object> content, Integer orderIndex) {
        this.homeworkId = homeworkId;
        this.questionType = questionType;
        this.content = content;
        this.orderIndex = orderIndex;
    }
    
    public Question(UUID homeworkId, String questionType, Map<String, Object> content, Integer orderIndex, String explanation, String videoUrl) {
        this.homeworkId = homeworkId;
        this.questionType = questionType;
        this.content = content;
        this.orderIndex = orderIndex;
        this.explanation = explanation;
        this.videoUrl = videoUrl;
    }
    
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
    
    // Getter和Setter方法
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getHomeworkId() {
        return homeworkId;
    }
    
    public void setHomeworkId(UUID homeworkId) {
        this.homeworkId = homeworkId;
    }
    
    public String getQuestionType() {
        return questionType;
    }
    
    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }
    
    public Map<String, Object> getContent() {
        return content;
    }
    
    public void setContent(Map<String, Object> content) {
        this.content = content;
    }
    
    public Map<String, Object> getStandardAnswer() {
        return standardAnswer;
    }
    
    public void setStandardAnswer(Map<String, Object> standardAnswer) {
        this.standardAnswer = standardAnswer;
    }
    
    public Integer getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
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
    
    public Homework getHomework() {
        return homework;
    }
    
    public void setHomework(Homework homework) {
        this.homework = homework;
        if (homework != null) {
            this.homeworkId = homework.getId();
        } else {
            this.homeworkId = null;
        }
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public String getVideoUrl() {
        return videoUrl;
    }
    
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    
    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", homeworkId=" + homeworkId +
                ", questionType='" + questionType + '\'' +
                ", orderIndex=" + orderIndex +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return id != null && id.equals(question.getId());
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}