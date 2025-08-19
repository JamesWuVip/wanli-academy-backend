package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Submission 实体测试类
 */
class SubmissionTest {

    private Submission submission;
    private Assignment assignment;
    private User student;
    private User grader;
    private UUID assignmentId;
    private Long studentId;
    private Long graderId;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        
        assignmentId = UUID.randomUUID();
        studentId = 1L;
        graderId = 2L;
        
        assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setTitle("Test Assignment");
        
        student = new User();
        student.setId(studentId);
        student.setUsername("student");
        
        grader = new User();
        grader.setId(graderId);
        grader.setUsername("teacher");
    }

    @Test
    void should_create_submission_with_default_constructor() {
        Submission newSubmission = new Submission();
        
        assertNull(newSubmission.getId());
        assertNull(newSubmission.getAssignmentId());
        assertNull(newSubmission.getStudentId());
        assertNull(newSubmission.getContent());
        assertNull(newSubmission.getFilePath());
        assertNull(newSubmission.getScore());
        assertNull(newSubmission.getFeedback());
        assertEquals("SUBMITTED", newSubmission.getStatus()); // 默认状态
        assertNull(newSubmission.getSubmittedAt());
        assertNull(newSubmission.getGradedAt());
        assertNull(newSubmission.getGradedBy());
        assertNull(newSubmission.getCreatedAt());
        assertNull(newSubmission.getUpdatedAt());
        assertNull(newSubmission.getAssignment());
        assertNull(newSubmission.getStudent());
        assertNull(newSubmission.getGrader());
    }

    @Test
    void should_create_submission_with_parameters() {
        String content = "This is my submission content";
        
        Submission newSubmission = new Submission(assignmentId, studentId, content);
        
        assertEquals(assignmentId, newSubmission.getAssignmentId());
        assertEquals(studentId, newSubmission.getStudentId());
        assertEquals(content, newSubmission.getContent());
        assertEquals("SUBMITTED", newSubmission.getStatus()); // 默认状态
    }

    @Test
    void should_set_and_get_all_properties() {
        UUID id = UUID.randomUUID();
        String content = "Test submission content";
        String filePath = "/path/to/file.pdf";
        Integer score = 85;
        String feedback = "Good work!";
        String status = "GRADED";
        LocalDateTime submittedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime gradedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime createdAt = LocalDateTime.now().minusHours(3);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        submission.setId(id);
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setContent(content);
        submission.setFilePath(filePath);
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus(status);
        submission.setSubmittedAt(submittedAt);
        submission.setGradedAt(gradedAt);
        submission.setGradedBy(graderId);
        submission.setCreatedAt(createdAt);
        submission.setUpdatedAt(updatedAt);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setGrader(grader);
        
        assertEquals(id, submission.getId());
        assertEquals(assignmentId, submission.getAssignmentId());
        assertEquals(studentId, submission.getStudentId());
        assertEquals(content, submission.getContent());
        assertEquals(filePath, submission.getFilePath());
        assertEquals(score, submission.getScore());
        assertEquals(feedback, submission.getFeedback());
        assertEquals(status, submission.getStatus());
        assertEquals(submittedAt, submission.getSubmittedAt());
        assertEquals(gradedAt, submission.getGradedAt());
        assertEquals(graderId, submission.getGradedBy());
        assertEquals(createdAt, submission.getCreatedAt());
        assertEquals(updatedAt, submission.getUpdatedAt());
        assertEquals(assignment, submission.getAssignment());
        assertEquals(student, submission.getStudent());
        assertEquals(grader, submission.getGrader());
    }

    @Test
    void should_call_onCreate_lifecycle_method() {
        assertNull(submission.getCreatedAt());
        assertNull(submission.getUpdatedAt());
        assertNull(submission.getSubmittedAt());
        
        submission.onCreate();
        
        assertNotNull(submission.getCreatedAt());
        assertNotNull(submission.getUpdatedAt());
        assertNotNull(submission.getSubmittedAt());
        // 比较到秒级精度，避免纳秒级差异
        assertEquals(submission.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), 
                    submission.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(submission.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), 
                    submission.getSubmittedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void should_not_override_existing_submittedAt_in_onCreate() {
        Submission newSubmission = new Submission();
        LocalDateTime existingSubmittedAt = LocalDateTime.now().minusHours(1);
        newSubmission.setSubmittedAt(existingSubmittedAt);
        
        // 模拟@PrePersist调用
        newSubmission.onCreate();
        
        // submittedAt不应该被覆盖
        assertEquals(existingSubmittedAt, newSubmission.getSubmittedAt());
        assertNotNull(newSubmission.getCreatedAt());
        assertNotNull(newSubmission.getUpdatedAt());
    }

    @Test
    void should_call_onUpdate_lifecycle_method() throws InterruptedException {
        submission.onCreate(); // 先设置创建时间
        LocalDateTime originalCreatedAt = submission.getCreatedAt();
        LocalDateTime originalUpdatedAt = submission.getUpdatedAt();
        LocalDateTime originalSubmittedAt = submission.getSubmittedAt();
        
        // 等待一小段时间确保时间戳不同
        Thread.sleep(10);
        
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        submission.onUpdate(); // 模拟@PreUpdate调用
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        
        assertEquals(originalCreatedAt, submission.getCreatedAt()); // 创建时间不变
        assertEquals(originalSubmittedAt, submission.getSubmittedAt()); // 提交时间不变
        assertNotEquals(originalUpdatedAt, submission.getUpdatedAt()); // 更新时间改变
        assertTrue(submission.getUpdatedAt().isAfter(beforeUpdate));
        assertTrue(submission.getUpdatedAt().isBefore(afterUpdate));
        assertTrue(submission.getUpdatedAt().isAfter(submission.getCreatedAt()));
    }

    @Test
    void should_return_correct_toString() {
        UUID id = UUID.randomUUID();
        submission.setId(id);
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setContent("Test content");
        submission.setFilePath("/path/to/file.pdf");
        submission.setScore(90);
        submission.setFeedback("Excellent work!");
        submission.setStatus("GRADED");
        LocalDateTime submittedAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        submission.setSubmittedAt(submittedAt);
        LocalDateTime gradedAt = LocalDateTime.of(2023, 1, 2, 10, 0);
        submission.setGradedAt(gradedAt);
        submission.setGradedBy(graderId);
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 9, 0);
        submission.setCreatedAt(createdAt);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0);
        submission.setUpdatedAt(updatedAt);
        
        String result = submission.toString();
        
        assertTrue(result.contains("Submission{"));
        assertTrue(result.contains("id=" + id));
        assertTrue(result.contains("assignmentId=" + assignmentId));
        assertTrue(result.contains("studentId=" + studentId));
        assertTrue(result.contains("content='Test content'"));
        assertTrue(result.contains("filePath='/path/to/file.pdf'"));
        assertTrue(result.contains("score=90"));
        assertTrue(result.contains("feedback='Excellent work!'"));
        assertTrue(result.contains("status='GRADED'"));
        assertTrue(result.contains("submittedAt=2023-01-01T10:00"));
        assertTrue(result.contains("gradedAt=2023-01-02T10:00"));
        assertTrue(result.contains("gradedBy=" + graderId));
        assertTrue(result.contains("createdAt=2023-01-01T09:00"));
        assertTrue(result.contains("updatedAt=2023-01-02T11:00"));
    }

    @Test
    void should_handle_null_values() {
        submission.setId(null);
        submission.setAssignmentId(null);
        submission.setStudentId(null);
        submission.setContent(null);
        submission.setFilePath(null);
        submission.setScore(null);
        submission.setFeedback(null);
        submission.setStatus(null);
        submission.setSubmittedAt(null);
        submission.setGradedAt(null);
        submission.setGradedBy(null);
        submission.setCreatedAt(null);
        submission.setUpdatedAt(null);
        submission.setAssignment(null);
        submission.setStudent(null);
        submission.setGrader(null);
        
        assertNull(submission.getId());
        assertNull(submission.getAssignmentId());
        assertNull(submission.getStudentId());
        assertNull(submission.getContent());
        assertNull(submission.getFilePath());
        assertNull(submission.getScore());
        assertNull(submission.getFeedback());
        assertNull(submission.getStatus());
        assertNull(submission.getSubmittedAt());
        assertNull(submission.getGradedAt());
        assertNull(submission.getGradedBy());
        assertNull(submission.getCreatedAt());
        assertNull(submission.getUpdatedAt());
        assertNull(submission.getAssignment());
        assertNull(submission.getStudent());
        assertNull(submission.getGrader());
    }

    @Test
    void should_handle_empty_strings() {
        submission.setContent("");
        submission.setFilePath("");
        submission.setFeedback("");
        submission.setStatus("");
        
        assertEquals("", submission.getContent());
        assertEquals("", submission.getFilePath());
        assertEquals("", submission.getFeedback());
        assertEquals("", submission.getStatus());
    }

    @Test
    void should_handle_submission_status_values() {
        // 测试不同的状态值
        String[] statuses = {"SUBMITTED", "GRADED", "RETURNED"};
        
        for (String status : statuses) {
            submission.setStatus(status);
            assertEquals(status, submission.getStatus());
        }
    }

    @Test
    void should_handle_score_scenarios() {
        // 测试正数分数
        submission.setScore(100);
        assertEquals(100, submission.getScore());
        
        // 测试零分
        submission.setScore(0);
        assertEquals(0, submission.getScore());
        
        // 测试负数分数（虽然业务上不合理，但测试数据完整性）
        submission.setScore(-10);
        assertEquals(-10, submission.getScore());
        
        // 测试null分数
        submission.setScore(null);
        assertNull(submission.getScore());
    }

    @Test
    void should_handle_uuid_generation() {
        // 测试UUID的唯一性
        Submission submission1 = new Submission();
        Submission submission2 = new Submission();
        
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID assignmentId1 = UUID.randomUUID();
        UUID assignmentId2 = UUID.randomUUID();
        
        submission1.setId(id1);
        submission1.setAssignmentId(assignmentId1);
        submission2.setId(id2);
        submission2.setAssignmentId(assignmentId2);
        
        assertNotEquals(submission1.getId(), submission2.getId());
        assertNotEquals(submission1.getAssignmentId(), submission2.getAssignmentId());
        assertNotNull(submission1.getId());
        assertNotNull(submission2.getId());
        assertNotNull(submission1.getAssignmentId());
        assertNotNull(submission2.getAssignmentId());
    }

    @Test
    void should_handle_grading_workflow() {
        // 模拟完整的评分工作流
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setContent("My submission");
        submission.setStatus("SUBMITTED");
        
        // 初始状态
        assertEquals("SUBMITTED", submission.getStatus());
        assertNull(submission.getScore());
        assertNull(submission.getFeedback());
        assertNull(submission.getGradedAt());
        assertNull(submission.getGradedBy());
        
        // 评分后
        submission.setScore(85);
        submission.setFeedback("Good work, but could be improved");
        submission.setStatus("GRADED");
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(graderId);
        submission.setGrader(grader);
        
        assertEquals("GRADED", submission.getStatus());
        assertEquals(85, submission.getScore());
        assertEquals("Good work, but could be improved", submission.getFeedback());
        assertNotNull(submission.getGradedAt());
        assertEquals(graderId, submission.getGradedBy());
        assertEquals(grader, submission.getGrader());
    }

    @Test
    void should_handle_file_submission() {
        String filePath = "/uploads/submissions/assignment1/student1/document.pdf";
        
        submission.setFilePath(filePath);
        submission.setContent("Please see attached file");
        
        assertEquals(filePath, submission.getFilePath());
        assertEquals("Please see attached file", submission.getContent());
    }

    @Test
    void should_handle_text_only_submission() {
        String content = "This is a text-only submission with detailed explanation...";
        
        submission.setContent(content);
        submission.setFilePath(null);
        
        assertEquals(content, submission.getContent());
        assertNull(submission.getFilePath());
    }

    @Test
    void should_handle_late_submission() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lateSubmissionTime = now.plusDays(1); // 假设这是迟交
        
        submission.setSubmittedAt(lateSubmissionTime);
        
        assertEquals(lateSubmissionTime, submission.getSubmittedAt());
        assertTrue(submission.getSubmittedAt().isAfter(now));
    }

    @Test
    void should_maintain_entity_relationships() {
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setGrader(grader);
        
        assertEquals(assignment, submission.getAssignment());
        assertEquals(student, submission.getStudent());
        assertEquals(grader, submission.getGrader());
        
        // 验证ID关系
        submission.setAssignmentId(assignment.getId());
        submission.setStudentId(student.getId());
        submission.setGradedBy(grader.getId());
        
        assertEquals(assignment.getId(), submission.getAssignmentId());
        assertEquals(student.getId(), submission.getStudentId());
        assertEquals(grader.getId(), submission.getGradedBy());
    }
}