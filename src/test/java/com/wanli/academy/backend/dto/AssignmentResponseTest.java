package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentResponseTest {

    @Test
    void should_createInstanceWithDefaultConstructor() {
        AssignmentResponse response = new AssignmentResponse();
        assertNotNull(response);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        String title = "数学作业第一章";
        String description = "完成第一章的所有练习题，包括基础题和提高题";
        Long creatorId = 1L;
        String creatorUsername = "张老师";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        Integer totalScore = 100;
        String status = "PUBLISHED";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        AssignmentResponse response = new AssignmentResponse(
                id, title, description, creatorId, creatorUsername,
                dueDate, totalScore, status, createdAt, updatedAt
        );

        assertEquals(id, response.getId());
        assertEquals(title, response.getTitle());
        assertEquals(description, response.getDescription());
        assertEquals(creatorId, response.getCreatorId());
        assertEquals(creatorUsername, response.getCreatorUsername());
        assertEquals(dueDate, response.getDueDate());
        assertEquals(totalScore, response.getTotalScore());
        assertEquals(status, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        AssignmentResponse response = new AssignmentResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    void should_setAndGetTitle() {
        AssignmentResponse response = new AssignmentResponse();
        String title = "数学作业第一章";
        
        response.setTitle(title);
        
        assertEquals(title, response.getTitle());
    }

    @Test
    void should_setAndGetDescription() {
        AssignmentResponse response = new AssignmentResponse();
        String description = "完成第一章的所有练习题，包括基础题和提高题";
        
        response.setDescription(description);
        
        assertEquals(description, response.getDescription());
    }

    @Test
    void should_setAndGetCreatorId() {
        AssignmentResponse response = new AssignmentResponse();
        Long creatorId = 1L;
        
        response.setCreatorId(creatorId);
        
        assertEquals(creatorId, response.getCreatorId());
    }

    @Test
    void should_setAndGetCreatorUsername() {
        AssignmentResponse response = new AssignmentResponse();
        String username = "张老师";
        
        response.setCreatorUsername(username);
        
        assertEquals(username, response.getCreatorUsername());
    }

    @Test
    void should_setAndGetDueDate() {
        AssignmentResponse response = new AssignmentResponse();
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        
        response.setDueDate(dueDate);
        
        assertEquals(dueDate, response.getDueDate());
    }

    @Test
    void should_setAndGetTotalScore() {
        AssignmentResponse response = new AssignmentResponse();
        Integer totalScore = 100;
        
        response.setTotalScore(totalScore);
        
        assertEquals(totalScore, response.getTotalScore());
    }

    @Test
    void should_setAndGetStatus() {
        AssignmentResponse response = new AssignmentResponse();
        String status = "PUBLISHED";
        
        response.setStatus(status);
        
        assertEquals(status, response.getStatus());
    }

    @Test
    void should_setAndGetCreatedAt() {
        AssignmentResponse response = new AssignmentResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setCreatedAt(createdAt);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        AssignmentResponse response = new AssignmentResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        response.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetSubmissions() {
        AssignmentResponse response = new AssignmentResponse();
        List<SubmissionResponse> submissions = new ArrayList<>();
        
        response.setSubmissions(submissions);
        
        assertEquals(submissions, response.getSubmissions());
    }

    @Test
    void should_setAndGetFiles() {
        AssignmentResponse response = new AssignmentResponse();
        List<AssignmentFileResponse> files = new ArrayList<>();
        
        response.setFiles(files);
        
        assertEquals(files, response.getFiles());
    }

    @Test
    void should_returnCorrectToString_withNullLists() {
        UUID id = UUID.randomUUID();
        String title = "数学作业第一章";
        String description = "完成第一章的所有练习题";
        Long creatorId = 1L;
        String creatorUsername = "张老师";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(7);
        Integer totalScore = 100;
        String status = "PUBLISHED";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        AssignmentResponse response = new AssignmentResponse(
                id, title, description, creatorId, creatorUsername,
                dueDate, totalScore, status, createdAt, updatedAt
        );

        String toString = response.toString();

        assertTrue(toString.contains("AssignmentResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("title='" + title + "'"));
        assertTrue(toString.contains("description='" + description + "'"));
        assertTrue(toString.contains("creatorId=" + creatorId));
        assertTrue(toString.contains("creatorUsername='" + creatorUsername + "'"));
        assertTrue(toString.contains("dueDate=" + dueDate));
        assertTrue(toString.contains("totalScore=" + totalScore));
        assertTrue(toString.contains("status='" + status + "'"));
        assertTrue(toString.contains("createdAt=" + createdAt));
        assertTrue(toString.contains("updatedAt=" + updatedAt));
        assertTrue(toString.contains("submissionsCount=0"));
        assertTrue(toString.contains("filesCount=0"));
    }

    @Test
    void should_returnCorrectToString_withNonEmptyLists() {
        AssignmentResponse response = new AssignmentResponse();
        
        List<SubmissionResponse> submissions = new ArrayList<>();
        submissions.add(new SubmissionResponse());
        submissions.add(new SubmissionResponse());
        
        List<AssignmentFileResponse> files = new ArrayList<>();
        files.add(new AssignmentFileResponse());
        
        response.setSubmissions(submissions);
        response.setFiles(files);

        String toString = response.toString();

        assertTrue(toString.contains("submissionsCount=2"));
        assertTrue(toString.contains("filesCount=1"));
    }

    @Test
    void should_handleNullValues() {
        AssignmentResponse response = new AssignmentResponse();
        
        assertNull(response.getId());
        assertNull(response.getTitle());
        assertNull(response.getDescription());
        assertNull(response.getCreatorId());
        assertNull(response.getCreatorUsername());
        assertNull(response.getDueDate());
        assertNull(response.getTotalScore());
        assertNull(response.getStatus());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
        assertNull(response.getSubmissions());
        assertNull(response.getFiles());
    }

    @Test
    void should_handleEmptyLists() {
        AssignmentResponse response = new AssignmentResponse();
        List<SubmissionResponse> emptySubmissions = new ArrayList<>();
        List<AssignmentFileResponse> emptyFiles = new ArrayList<>();
        
        response.setSubmissions(emptySubmissions);
        response.setFiles(emptyFiles);
        
        assertEquals(0, response.getSubmissions().size());
        assertEquals(0, response.getFiles().size());
        
        String toString = response.toString();
        assertTrue(toString.contains("submissionsCount=0"));
        assertTrue(toString.contains("filesCount=0"));
    }
}