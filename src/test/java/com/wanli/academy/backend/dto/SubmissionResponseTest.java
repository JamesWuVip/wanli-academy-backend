package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionResponseTest {

    @Test
    void should_createInstanceWithDefaultConstructor() {
        SubmissionResponse response = new SubmissionResponse();
        assertNotNull(response);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        String assignmentTitle = "数学作业第一章";
        Long studentId = 2L;
        String studentUsername = "张三";
        String content = "这是我的作业答案...";
        String filePath = "/uploads/submissions/homework1_zhangsan.pdf";
        Integer score = 85;
        String feedback = "答案基本正确，但需要注意计算细节";
        String status = "SUBMITTED";
        LocalDateTime submittedAt = LocalDateTime.now();
        LocalDateTime gradedAt = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now().plusHours(1);

        SubmissionResponse response = new SubmissionResponse(
                id, assignmentId, assignmentTitle, studentId, studentUsername, content, filePath,
                score, feedback, status, submittedAt, gradedAt, createdAt, updatedAt
        );

        assertEquals(id, response.getId());
        assertEquals(assignmentId, response.getAssignmentId());
        assertEquals(assignmentTitle, response.getAssignmentTitle());
        assertEquals(studentId, response.getStudentId());
        assertEquals(studentUsername, response.getStudentUsername());
        assertEquals(content, response.getContent());
        assertEquals(filePath, response.getFilePath());
        assertEquals(score, response.getScore());
        assertEquals(feedback, response.getFeedback());
        assertEquals(status, response.getStatus());
        assertEquals(submittedAt, response.getSubmittedAt());
        assertEquals(gradedAt, response.getGradedAt());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        SubmissionResponse response = new SubmissionResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    void should_setAndGetAssignmentId() {
        SubmissionResponse response = new SubmissionResponse();
        UUID assignmentId = UUID.randomUUID();
        
        response.setAssignmentId(assignmentId);
        
        assertEquals(assignmentId, response.getAssignmentId());
    }

    @Test
    void should_setAndGetAssignmentTitle() {
        SubmissionResponse response = new SubmissionResponse();
        String assignmentTitle = "数学作业第一章";
        
        response.setAssignmentTitle(assignmentTitle);
        
        assertEquals(assignmentTitle, response.getAssignmentTitle());
    }

    @Test
    void should_setAndGetStudentId() {
        SubmissionResponse response = new SubmissionResponse();
        Long studentId = 2L;
        
        response.setStudentId(studentId);
        
        assertEquals(studentId, response.getStudentId());
    }

    @Test
    void should_setAndGetStudentUsername() {
        SubmissionResponse response = new SubmissionResponse();
        String studentUsername = "张三";
        
        response.setStudentUsername(studentUsername);
        
        assertEquals(studentUsername, response.getStudentUsername());
    }

    @Test
    void should_setAndGetContent() {
        SubmissionResponse response = new SubmissionResponse();
        String content = "这是我的作业答案...";
        
        response.setContent(content);
        
        assertEquals(content, response.getContent());
    }

    @Test
    void should_setAndGetFilePath() {
        SubmissionResponse response = new SubmissionResponse();
        String filePath = "/uploads/submissions/homework1_zhangsan.pdf";
        
        response.setFilePath(filePath);
        
        assertEquals(filePath, response.getFilePath());
    }

    @Test
    void should_setAndGetScore() {
        SubmissionResponse response = new SubmissionResponse();
        Integer score = 85;
        
        response.setScore(score);
        
        assertEquals(score, response.getScore());
    }

    @Test
    void should_setAndGetFeedback() {
        SubmissionResponse response = new SubmissionResponse();
        String feedback = "答案基本正确，但需要注意计算细节";
        
        response.setFeedback(feedback);
        
        assertEquals(feedback, response.getFeedback());
    }

    @Test
    void should_setAndGetStatus() {
        SubmissionResponse response = new SubmissionResponse();
        String status = "SUBMITTED";
        
        response.setStatus(status);
        
        assertEquals(status, response.getStatus());
    }

    @Test
    void should_setAndGetSubmittedAt() {
        SubmissionResponse response = new SubmissionResponse();
        LocalDateTime submittedAt = LocalDateTime.now();
        
        response.setSubmittedAt(submittedAt);
        
        assertEquals(submittedAt, response.getSubmittedAt());
    }

    @Test
    void should_setAndGetGradedAt() {
        SubmissionResponse response = new SubmissionResponse();
        LocalDateTime gradedAt = LocalDateTime.now();
        
        response.setGradedAt(gradedAt);
        
        assertEquals(gradedAt, response.getGradedAt());
    }

    @Test
    void should_setAndGetCreatedAt() {
        SubmissionResponse response = new SubmissionResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setCreatedAt(createdAt);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        SubmissionResponse response = new SubmissionResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        response.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_returnCorrectToStringWithShortContent() {
        UUID id = UUID.randomUUID();
        String content = "短内容";
        String feedback = "简短反馈";
        SubmissionResponse response = new SubmissionResponse();
        response.setId(id);
        response.setContent(content);
        response.setFeedback(feedback);

        String toString = response.toString();

        assertTrue(toString.contains("SubmissionResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("content='" + content + "'"));
        assertTrue(toString.contains("feedback='" + feedback + "'"));
    }

    @Test
    void should_returnCorrectToStringWithLongContent() {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        String longContent = "This is a very long content that exceeds fifty characters and should be truncated in toString method";
        response.setContent(longContent);
        
        String result = response.toString();
        
        // 验证内容被截断
        assertTrue(result.contains("content='This is a very long content that exceeds fifty cha..."));
        // 验证不包含完整的长内容
        assertFalse(result.contains(longContent));
    }

    @Test
    void should_returnCorrectToStringWithLongFeedback() {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        String longFeedback = "This is a very long feedback that exceeds fifty characters and should be truncated in toString method";
        response.setFeedback(longFeedback);
        
        String result = response.toString();
        
        // 验证反馈被截断
        assertTrue(result.contains("feedback='This is a very long feedback that exceeds fifty ch..."));
        // 验证不包含完整的长反馈
        assertFalse(result.contains(longFeedback));
    }

    @Test
    void should_returnCorrectToStringWithExactly50CharContent() {
        UUID id = UUID.randomUUID();
        String content50 = "a".repeat(50); // 正好50个字符
        SubmissionResponse response = new SubmissionResponse();
        response.setId(id);
        response.setContent(content50);

        String toString = response.toString();

        assertTrue(toString.contains("SubmissionResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("content='" + content50 + "'"));
        assertFalse(toString.contains("..."));
    }

    @Test
    void should_returnCorrectToStringWithExactly50CharFeedback() {
        UUID id = UUID.randomUUID();
        String feedback50 = "b".repeat(50); // 正好50个字符
        SubmissionResponse response = new SubmissionResponse();
        response.setId(id);
        response.setFeedback(feedback50);

        String toString = response.toString();

        assertTrue(toString.contains("SubmissionResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("feedback='" + feedback50 + "'"));
        assertFalse(toString.contains("..."));
    }

    @Test
    void should_handleNullValues() {
        SubmissionResponse response = new SubmissionResponse();
        
        assertNull(response.getId());
        assertNull(response.getAssignmentId());
        assertNull(response.getAssignmentTitle());
        assertNull(response.getStudentId());
        assertNull(response.getStudentUsername());
        assertNull(response.getContent());
        assertNull(response.getFilePath());
        assertNull(response.getScore());
        assertNull(response.getFeedback());
        assertNull(response.getStatus());
        assertNull(response.getSubmittedAt());
        assertNull(response.getGradedAt());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void should_handleEmptyStrings() {
        SubmissionResponse response = new SubmissionResponse();
        
        response.setAssignmentTitle("");
        response.setStudentUsername("");
        response.setContent("");
        response.setFilePath("");
        response.setFeedback("");
        response.setStatus("");
        
        assertEquals("", response.getAssignmentTitle());
        assertEquals("", response.getStudentUsername());
        assertEquals("", response.getContent());
        assertEquals("", response.getFilePath());
        assertEquals("", response.getFeedback());
        assertEquals("", response.getStatus());
    }

    @Test
    void should_handleZeroScore() {
        SubmissionResponse response = new SubmissionResponse();
        
        response.setScore(0);
        
        assertEquals(0, response.getScore());
    }

    @Test
    void should_handleNegativeScore() {
        SubmissionResponse response = new SubmissionResponse();
        
        response.setScore(-1);
        
        assertEquals(-1, response.getScore());
    }

    @Test
    void should_handleMaxScore() {
        SubmissionResponse response = new SubmissionResponse();
        
        response.setScore(100);
        
        assertEquals(100, response.getScore());
    }

    @Test
    void should_handleLargeScore() {
        SubmissionResponse response = new SubmissionResponse();
        
        response.setScore(Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, response.getScore());
    }

    @Test
    void should_handleDifferentStatuses() {
        String[] statuses = {"DRAFT", "SUBMITTED", "GRADED", "RETURNED"};
        
        for (String status : statuses) {
            SubmissionResponse response = new SubmissionResponse();
            response.setStatus(status);
            
            assertEquals(status, response.getStatus());
        }
    }

    @Test
    void should_handleNullContentInToString() {
        SubmissionResponse response = new SubmissionResponse();
        response.setContent(null);
        response.setFeedback(null);

        String toString = response.toString();

        assertTrue(toString.contains("content='null'"));
        assertTrue(toString.contains("feedback='null'"));
    }

    @Test
    void should_handleTimeSequence() {
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime submittedAt = createdAt.plusHours(1);
        LocalDateTime gradedAt = submittedAt.plusHours(2);
        LocalDateTime updatedAt = gradedAt.plusMinutes(30);
        
        SubmissionResponse response = new SubmissionResponse();
        response.setCreatedAt(createdAt);
        response.setSubmittedAt(submittedAt);
        response.setGradedAt(gradedAt);
        response.setUpdatedAt(updatedAt);
        
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(submittedAt, response.getSubmittedAt());
        assertEquals(gradedAt, response.getGradedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        
        assertTrue(submittedAt.isAfter(createdAt));
        assertTrue(gradedAt.isAfter(submittedAt));
        assertTrue(updatedAt.isAfter(gradedAt));
    }
}