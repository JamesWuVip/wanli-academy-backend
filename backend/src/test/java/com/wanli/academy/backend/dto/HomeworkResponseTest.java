package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HomeworkResponseTest {

    @Test
    void should_createInstanceWithDefaultConstructor() {
        HomeworkResponse response = new HomeworkResponse();
        assertNotNull(response);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        String title = "数学作业第一章";
        String description = "完成第一章的所有练习题，包括基础题和提高题";
        Long createdBy = 1L;
        String createdByUsername = "张老师";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        HomeworkResponse response = new HomeworkResponse(
                id, title, description, createdBy, createdByUsername, createdAt, updatedAt
        );

        assertEquals(id, response.getId());
        assertEquals(title, response.getTitle());
        assertEquals(description, response.getDescription());
        assertEquals(createdBy, response.getCreatedBy());
        assertEquals(createdByUsername, response.getCreatedByUsername());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        HomeworkResponse response = new HomeworkResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    void should_setAndGetTitle() {
        HomeworkResponse response = new HomeworkResponse();
        String title = "数学作业第一章";
        
        response.setTitle(title);
        
        assertEquals(title, response.getTitle());
    }

    @Test
    void should_setAndGetDescription() {
        HomeworkResponse response = new HomeworkResponse();
        String description = "完成第一章的所有练习题，包括基础题和提高题";
        
        response.setDescription(description);
        
        assertEquals(description, response.getDescription());
    }

    @Test
    void should_setAndGetCreatedBy() {
        HomeworkResponse response = new HomeworkResponse();
        Long createdBy = 1L;
        
        response.setCreatedBy(createdBy);
        
        assertEquals(createdBy, response.getCreatedBy());
    }

    @Test
    void should_setAndGetCreatedByUsername() {
        HomeworkResponse response = new HomeworkResponse();
        String createdByUsername = "张老师";
        
        response.setCreatedByUsername(createdByUsername);
        
        assertEquals(createdByUsername, response.getCreatedByUsername());
    }

    @Test
    void should_setAndGetCreatedAt() {
        HomeworkResponse response = new HomeworkResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setCreatedAt(createdAt);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        HomeworkResponse response = new HomeworkResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        response.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetQuestions() {
        HomeworkResponse response = new HomeworkResponse();
        List<QuestionResponse> questions = new ArrayList<>();
        
        response.setQuestions(questions);
        
        assertEquals(questions, response.getQuestions());
    }

    @Test
    void should_returnCorrectToStringWithNullQuestions() {
        UUID id = UUID.randomUUID();
        String title = "数学作业第一章";
        String description = "完成第一章的所有练习题";
        Long createdBy = 1L;
        String createdByUsername = "张老师";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        HomeworkResponse response = new HomeworkResponse(
                id, title, description, createdBy, createdByUsername, createdAt, updatedAt
        );

        String toString = response.toString();

        assertTrue(toString.contains("HomeworkResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("title='" + title + "'"));
        assertTrue(toString.contains("description='" + description + "'"));
        assertTrue(toString.contains("createdBy=" + createdBy));
        assertTrue(toString.contains("createdByUsername='" + createdByUsername + "'"));
        assertTrue(toString.contains("createdAt=" + createdAt));
        assertTrue(toString.contains("updatedAt=" + updatedAt));
        assertTrue(toString.contains("questionsCount=0"));
    }

    @Test
    void should_returnCorrectToStringWithEmptyQuestions() {
        UUID id = UUID.randomUUID();
        String title = "数学作业第一章";
        HomeworkResponse response = new HomeworkResponse();
        response.setId(id);
        response.setTitle(title);
        response.setQuestions(new ArrayList<>());

        String toString = response.toString();

        assertTrue(toString.contains("HomeworkResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("title='" + title + "'"));
        assertTrue(toString.contains("questionsCount=0"));
    }

    @Test
    void should_returnCorrectToStringWithQuestions() {
        UUID id = UUID.randomUUID();
        String title = "数学作业第一章";
        HomeworkResponse response = new HomeworkResponse();
        response.setId(id);
        response.setTitle(title);
        
        List<QuestionResponse> questions = new ArrayList<>();
        questions.add(new QuestionResponse()); // 添加一个空的QuestionResponse
        questions.add(new QuestionResponse()); // 添加另一个空的QuestionResponse
        response.setQuestions(questions);

        String toString = response.toString();

        assertTrue(toString.contains("HomeworkResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("title='" + title + "'"));
        assertTrue(toString.contains("questionsCount=2"));
    }

    @Test
    void should_handleNullValues() {
        HomeworkResponse response = new HomeworkResponse();
        
        assertNull(response.getId());
        assertNull(response.getTitle());
        assertNull(response.getDescription());
        assertNull(response.getCreatedBy());
        assertNull(response.getCreatedByUsername());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
        assertNull(response.getQuestions());
    }

    @Test
    void should_handleEmptyStrings() {
        HomeworkResponse response = new HomeworkResponse();
        
        response.setTitle("");
        response.setDescription("");
        response.setCreatedByUsername("");
        
        assertEquals("", response.getTitle());
        assertEquals("", response.getDescription());
        assertEquals("", response.getCreatedByUsername());
    }

    @Test
    void should_handleZeroCreatedBy() {
        HomeworkResponse response = new HomeworkResponse();
        
        response.setCreatedBy(0L);
        
        assertEquals(0L, response.getCreatedBy());
    }

    @Test
    void should_handleNegativeCreatedBy() {
        HomeworkResponse response = new HomeworkResponse();
        
        response.setCreatedBy(-1L);
        
        assertEquals(-1L, response.getCreatedBy());
    }

    @Test
    void should_handleLargeQuestionsList() {
        HomeworkResponse response = new HomeworkResponse();
        List<QuestionResponse> questions = new ArrayList<>();
        
        // 添加100个问题
        for (int i = 0; i < 100; i++) {
            questions.add(new QuestionResponse());
        }
        response.setQuestions(questions);

        assertEquals(100, response.getQuestions().size());
        
        String toString = response.toString();
        assertTrue(toString.contains("questionsCount=100"));
    }

    @Test
    void should_maintainQuestionsListReference() {
        HomeworkResponse response = new HomeworkResponse();
        List<QuestionResponse> questions = new ArrayList<>();
        
        response.setQuestions(questions);
        
        // 修改原始列表
        questions.add(new QuestionResponse());
        
        // 响应对象中的列表也应该被修改
        assertEquals(1, response.getQuestions().size());
    }
}