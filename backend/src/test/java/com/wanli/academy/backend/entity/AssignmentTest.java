package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assignment 实体测试类
 */
class AssignmentTest {

    private Assignment assignment;
    private User creator;
    private Submission submission;
    private AssignmentFile assignmentFile;

    @BeforeEach
    void setUp() {
        assignment = new Assignment();
        
        creator = new User();
        creator.setId(1L);
        creator.setUsername("teacher");
        
        submission = new Submission();
        submission.setId(UUID.randomUUID());
        
        assignmentFile = new AssignmentFile();
        assignmentFile.setId(UUID.randomUUID());
    }

    @Test
    void should_create_assignment_with_default_constructor() {
        Assignment newAssignment = new Assignment();
        
        assertNull(newAssignment.getId());
        assertNull(newAssignment.getTitle());
        assertNull(newAssignment.getDescription());
        assertNull(newAssignment.getCreatorId());
        assertNull(newAssignment.getDueDate());
        assertNull(newAssignment.getMaxScore());
        assertEquals("DRAFT", newAssignment.getStatus()); // 默认状态
        assertNull(newAssignment.getCreatedAt());
        assertNull(newAssignment.getUpdatedAt());
        assertNull(newAssignment.getCreator());
        assertNotNull(newAssignment.getSubmissions());
        assertTrue(newAssignment.getSubmissions().isEmpty());
        assertNotNull(newAssignment.getFiles());
        assertTrue(newAssignment.getFiles().isEmpty());
    }

    @Test
    void should_create_assignment_with_parameters() {
        String title = "Test Assignment";
        String description = "This is a test assignment";
        Long creatorId = 1L;
        
        Assignment newAssignment = new Assignment(title, description, creatorId);
        
        assertEquals(title, newAssignment.getTitle());
        assertEquals(description, newAssignment.getDescription());
        assertEquals(creatorId, newAssignment.getCreatorId());
        assertEquals("DRAFT", newAssignment.getStatus()); // 默认状态
        assertNotNull(newAssignment.getSubmissions());
        assertTrue(newAssignment.getSubmissions().isEmpty());
        assertNotNull(newAssignment.getFiles());
        assertTrue(newAssignment.getFiles().isEmpty());
    }

    @Test
    void should_set_and_get_all_properties() {
        UUID id = UUID.randomUUID();
        String title = "Test Assignment";
        String description = "This is a test assignment";
        Long creatorId = 1L;
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        Integer maxScore = 100;
        String status = "PUBLISHED";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<Submission> submissions = new ArrayList<>();
        submissions.add(submission);
        List<AssignmentFile> files = new ArrayList<>();
        files.add(assignmentFile);
        
        assignment.setId(id);
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setCreatorId(creatorId);
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(maxScore);
        assignment.setStatus(status);
        assignment.setCreatedAt(createdAt);
        assignment.setUpdatedAt(updatedAt);
        assignment.setCreator(creator);
        assignment.setSubmissions(submissions);
        assignment.setFiles(files);
        
        assertEquals(id, assignment.getId());
        assertEquals(title, assignment.getTitle());
        assertEquals(description, assignment.getDescription());
        assertEquals(creatorId, assignment.getCreatorId());
        assertEquals(dueDate, assignment.getDueDate());
        assertEquals(maxScore, assignment.getMaxScore());
        assertEquals(status, assignment.getStatus());
        assertEquals(createdAt, assignment.getCreatedAt());
        assertEquals(updatedAt, assignment.getUpdatedAt());
        assertEquals(creator, assignment.getCreator());
        assertEquals(submissions, assignment.getSubmissions());
        assertEquals(files, assignment.getFiles());
    }

    @Test
    void should_add_submission_successfully() {
        assignment.addSubmission(submission);
        
        assertTrue(assignment.getSubmissions().contains(submission));
        assertEquals(assignment, submission.getAssignment());
        assertEquals(1, assignment.getSubmissions().size());
    }

    @Test
    void should_remove_submission_successfully() {
        // 先添加提交
        assignment.addSubmission(submission);
        assertTrue(assignment.getSubmissions().contains(submission));
        assertEquals(assignment, submission.getAssignment());
        
        // 然后移除提交
        assignment.removeSubmission(submission);
        
        assertFalse(assignment.getSubmissions().contains(submission));
        assertNull(submission.getAssignment());
        assertEquals(0, assignment.getSubmissions().size());
    }

    @Test
    void should_handle_multiple_submissions() {
        Submission submission2 = new Submission();
        submission2.setId(UUID.randomUUID());
        
        assignment.addSubmission(submission);
        assignment.addSubmission(submission2);
        
        assertEquals(2, assignment.getSubmissions().size());
        assertTrue(assignment.getSubmissions().contains(submission));
        assertTrue(assignment.getSubmissions().contains(submission2));
        assertEquals(assignment, submission.getAssignment());
        assertEquals(assignment, submission2.getAssignment());
    }

    @Test
    void should_add_file_successfully() {
        assignment.addFile(assignmentFile);
        
        assertTrue(assignment.getFiles().contains(assignmentFile));
        assertEquals(assignment, assignmentFile.getAssignment());
        assertEquals(1, assignment.getFiles().size());
    }

    @Test
    void should_remove_file_successfully() {
        // 先添加文件
        assignment.addFile(assignmentFile);
        assertTrue(assignment.getFiles().contains(assignmentFile));
        assertEquals(assignment, assignmentFile.getAssignment());
        
        // 然后移除文件
        assignment.removeFile(assignmentFile);
        
        assertFalse(assignment.getFiles().contains(assignmentFile));
        assertNull(assignmentFile.getAssignment());
        assertEquals(0, assignment.getFiles().size());
    }

    @Test
    void should_handle_multiple_files() {
        AssignmentFile file2 = new AssignmentFile();
        file2.setId(UUID.randomUUID());
        
        assignment.addFile(assignmentFile);
        assignment.addFile(file2);
        
        assertEquals(2, assignment.getFiles().size());
        assertTrue(assignment.getFiles().contains(assignmentFile));
        assertTrue(assignment.getFiles().contains(file2));
        assertEquals(assignment, assignmentFile.getAssignment());
        assertEquals(assignment, file2.getAssignment());
    }

    @Test
    void should_call_onCreate_lifecycle_method() {
        assertNull(assignment.getCreatedAt());
        assertNull(assignment.getUpdatedAt());
        
        assignment.onCreate();
        
        assertNotNull(assignment.getCreatedAt());
        assertNotNull(assignment.getUpdatedAt());
        // 比较到秒级精度，避免纳秒级差异
        assertEquals(assignment.getCreatedAt().truncatedTo(ChronoUnit.SECONDS), 
                    assignment.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void should_call_onUpdate_lifecycle_method() throws InterruptedException {
        assignment.onCreate(); // 先设置创建时间
        LocalDateTime originalCreatedAt = assignment.getCreatedAt();
        LocalDateTime originalUpdatedAt = assignment.getUpdatedAt();
        
        // 等待一小段时间确保时间戳不同
        Thread.sleep(10);
        
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        assignment.onUpdate(); // 模拟@PreUpdate调用
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);
        
        assertEquals(originalCreatedAt, assignment.getCreatedAt()); // 创建时间不变
        assertNotEquals(originalUpdatedAt, assignment.getUpdatedAt()); // 更新时间改变
        assertTrue(assignment.getUpdatedAt().isAfter(beforeUpdate));
        assertTrue(assignment.getUpdatedAt().isBefore(afterUpdate));
        assertTrue(assignment.getUpdatedAt().isAfter(assignment.getCreatedAt()));
    }

    @Test
    void should_return_correct_toString() {
        UUID id = UUID.randomUUID();
        assignment.setId(id);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Test Description");
        assignment.setCreatorId(1L);
        LocalDateTime dueDate = LocalDateTime.of(2023, 12, 31, 23, 59);
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(100);
        assignment.setStatus("PUBLISHED");
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        assignment.setCreatedAt(createdAt);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 10, 0);
        assignment.setUpdatedAt(updatedAt);
        
        String result = assignment.toString();
        
        assertTrue(result.contains("Assignment{"));
        assertTrue(result.contains("id=" + id));
        assertTrue(result.contains("title='Test Assignment'"));
        assertTrue(result.contains("description='Test Description'"));
        assertTrue(result.contains("creatorId=1"));
        assertTrue(result.contains("dueDate=2023-12-31T23:59"));
        assertTrue(result.contains("maxScore=100"));
        assertTrue(result.contains("status='PUBLISHED'"));
        assertTrue(result.contains("createdAt=2023-01-01T10:00"));
        assertTrue(result.contains("updatedAt=2023-01-02T10:00"));
    }

    @Test
    void should_handle_null_values() {
        assignment.setId(null);
        assignment.setTitle(null);
        assignment.setDescription(null);
        assignment.setCreatorId(null);
        assignment.setDueDate(null);
        assignment.setMaxScore(null);
        assignment.setStatus(null);
        assignment.setCreatedAt(null);
        assignment.setUpdatedAt(null);
        assignment.setCreator(null);
        assignment.setSubmissions(null);
        assignment.setFiles(null);
        
        assertNull(assignment.getId());
        assertNull(assignment.getTitle());
        assertNull(assignment.getDescription());
        assertNull(assignment.getCreatorId());
        assertNull(assignment.getDueDate());
        assertNull(assignment.getMaxScore());
        assertNull(assignment.getStatus());
        assertNull(assignment.getCreatedAt());
        assertNull(assignment.getUpdatedAt());
        assertNull(assignment.getCreator());
        assertNull(assignment.getSubmissions());
        assertNull(assignment.getFiles());
    }

    @Test
    void should_handle_empty_strings() {
        assignment.setTitle("");
        assignment.setDescription("");
        assignment.setStatus("");
        
        assertEquals("", assignment.getTitle());
        assertEquals("", assignment.getDescription());
        assertEquals("", assignment.getStatus());
    }

    @Test
    void should_handle_assignment_status_values() {
        // 测试不同的状态值
        String[] statuses = {"DRAFT", "PUBLISHED", "CLOSED"};
        
        for (String status : statuses) {
            assignment.setStatus(status);
            assertEquals(status, assignment.getStatus());
        }
    }

    @Test
    void should_maintain_collections_reference() {
        List<Submission> submissions = new ArrayList<>();
        submissions.add(submission);
        
        List<AssignmentFile> files = new ArrayList<>();
        files.add(assignmentFile);
        
        assignment.setSubmissions(submissions);
        assignment.setFiles(files);
        
        assertSame(submissions, assignment.getSubmissions());
        assertSame(files, assignment.getFiles());
        
        // 修改原始集合应该反映在Assignment中
        Submission newSubmission = new Submission();
        newSubmission.setId(UUID.randomUUID());
        submissions.add(newSubmission);
        
        AssignmentFile newFile = new AssignmentFile();
        newFile.setId(UUID.randomUUID());
        files.add(newFile);
        
        assertEquals(2, assignment.getSubmissions().size());
        assertEquals(2, assignment.getFiles().size());
        assertTrue(assignment.getSubmissions().contains(newSubmission));
        assertTrue(assignment.getFiles().contains(newFile));
    }

    @Test
    void should_handle_uuid_generation() {
        // 测试UUID的唯一性
        Assignment assignment1 = new Assignment();
        Assignment assignment2 = new Assignment();
        
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        
        assignment1.setId(id1);
        assignment2.setId(id2);
        
        assertNotEquals(assignment1.getId(), assignment2.getId());
        assertNotNull(assignment1.getId());
        assertNotNull(assignment2.getId());
    }

    @Test
    void should_handle_due_date_scenarios() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(7);
        LocalDateTime past = now.minusDays(7);
        
        // 测试未来日期
        assignment.setDueDate(future);
        assertEquals(future, assignment.getDueDate());
        assertTrue(assignment.getDueDate().isAfter(now));
        
        // 测试过去日期
        assignment.setDueDate(past);
        assertEquals(past, assignment.getDueDate());
        assertTrue(assignment.getDueDate().isBefore(now));
        
        // 测试null日期
        assignment.setDueDate(null);
        assertNull(assignment.getDueDate());
    }

    @Test
    void should_handle_max_score_scenarios() {
        // 测试正数分数
        assignment.setMaxScore(100);
        assertEquals(100, assignment.getMaxScore());
        
        // 测试零分
        assignment.setMaxScore(0);
        assertEquals(0, assignment.getMaxScore());
        
        // 测试负数分数（虽然业务上不合理，但测试数据完整性）
        assignment.setMaxScore(-10);
        assertEquals(-10, assignment.getMaxScore());
        
        // 测试null分数
        assignment.setMaxScore(null);
        assertNull(assignment.getMaxScore());
    }
}