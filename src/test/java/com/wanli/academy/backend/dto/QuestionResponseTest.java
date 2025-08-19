package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuestionResponseTest {

    @Test
    void should_createInstanceWithDefaultConstructor() {
        QuestionResponse response = new QuestionResponse();
        assertNotNull(response);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        String content = "计算 2 + 3 = ?";
        String questionType = "选择题";
        String standardAnswer = "5";
        Integer orderIndex = 1;
        UUID homeworkId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        QuestionResponse response = new QuestionResponse(
                id, content, questionType, standardAnswer, orderIndex, homeworkId, createdAt, updatedAt
        );

        assertEquals(id, response.getId());
        assertEquals(content, response.getContent());
        assertEquals(questionType, response.getQuestionType());
        assertEquals(standardAnswer, response.getStandardAnswer());
        assertEquals(orderIndex, response.getOrderIndex());
        assertEquals(homeworkId, response.getHomeworkId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        QuestionResponse response = new QuestionResponse();
        UUID id = UUID.randomUUID();
        
        response.setId(id);
        
        assertEquals(id, response.getId());
    }

    @Test
    void should_setAndGetContent() {
        QuestionResponse response = new QuestionResponse();
        String content = "计算 2 + 3 = ?";
        
        response.setContent(content);
        
        assertEquals(content, response.getContent());
    }

    @Test
    void should_setAndGetQuestionType() {
        QuestionResponse response = new QuestionResponse();
        String questionType = "选择题";
        
        response.setQuestionType(questionType);
        
        assertEquals(questionType, response.getQuestionType());
    }

    @Test
    void should_setAndGetStandardAnswer() {
        QuestionResponse response = new QuestionResponse();
        String standardAnswer = "5";
        
        response.setStandardAnswer(standardAnswer);
        
        assertEquals(standardAnswer, response.getStandardAnswer());
    }

    @Test
    void should_setAndGetOrderIndex() {
        QuestionResponse response = new QuestionResponse();
        Integer orderIndex = 1;
        
        response.setOrderIndex(orderIndex);
        
        assertEquals(orderIndex, response.getOrderIndex());
    }

    @Test
    void should_setAndGetHomeworkId() {
        QuestionResponse response = new QuestionResponse();
        UUID homeworkId = UUID.randomUUID();
        
        response.setHomeworkId(homeworkId);
        
        assertEquals(homeworkId, response.getHomeworkId());
    }

    @Test
    void should_setAndGetCreatedAt() {
        QuestionResponse response = new QuestionResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setCreatedAt(createdAt);
        
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        QuestionResponse response = new QuestionResponse();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        response.setUpdatedAt(updatedAt);
        
        assertEquals(updatedAt, response.getUpdatedAt());
    }

    @Test
    void should_returnCorrectToString() {
        UUID id = UUID.randomUUID();
        String content = "计算 2 + 3 = ?";
        String questionType = "选择题";
        String standardAnswer = "5";
        Integer orderIndex = 1;
        UUID homeworkId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        QuestionResponse response = new QuestionResponse(
                id, content, questionType, standardAnswer, orderIndex, homeworkId, createdAt, updatedAt
        );

        String toString = response.toString();

        assertTrue(toString.contains("QuestionResponse{"));
        assertTrue(toString.contains("id=" + id));
        assertTrue(toString.contains("content='" + content + "'"));
        assertTrue(toString.contains("questionType='" + questionType + "'"));
        assertTrue(toString.contains("standardAnswer='" + standardAnswer + "'"));
        assertTrue(toString.contains("orderIndex=" + orderIndex));
        assertTrue(toString.contains("homeworkId=" + homeworkId));
        assertTrue(toString.contains("createdAt=" + createdAt));
        assertTrue(toString.contains("updatedAt=" + updatedAt));
    }

    @Test
    void should_handleNullValues() {
        QuestionResponse response = new QuestionResponse();
        
        assertNull(response.getId());
        assertNull(response.getContent());
        assertNull(response.getQuestionType());
        assertNull(response.getStandardAnswer());
        assertNull(response.getOrderIndex());
        assertNull(response.getHomeworkId());
        assertNull(response.getCreatedAt());
        assertNull(response.getUpdatedAt());
    }

    @Test
    void should_handleEmptyStrings() {
        QuestionResponse response = new QuestionResponse();
        
        response.setContent("");
        response.setQuestionType("");
        response.setStandardAnswer("");
        
        assertEquals("", response.getContent());
        assertEquals("", response.getQuestionType());
        assertEquals("", response.getStandardAnswer());
    }

    @Test
    void should_handleZeroOrderIndex() {
        QuestionResponse response = new QuestionResponse();
        
        response.setOrderIndex(0);
        
        assertEquals(0, response.getOrderIndex());
    }

    @Test
    void should_handleNegativeOrderIndex() {
        QuestionResponse response = new QuestionResponse();
        
        response.setOrderIndex(-1);
        
        assertEquals(-1, response.getOrderIndex());
    }

    @Test
    void should_handleLargeOrderIndex() {
        QuestionResponse response = new QuestionResponse();
        
        response.setOrderIndex(Integer.MAX_VALUE);
        
        assertEquals(Integer.MAX_VALUE, response.getOrderIndex());
    }

    @Test
    void should_handleLongContent() {
        QuestionResponse response = new QuestionResponse();
        String longContent = "这是一个非常长的题目内容，".repeat(100);
        
        response.setContent(longContent);
        
        assertEquals(longContent, response.getContent());
    }

    @Test
    void should_handleLongStandardAnswer() {
        QuestionResponse response = new QuestionResponse();
        String longAnswer = "这是一个非常长的标准答案，".repeat(100);
        
        response.setStandardAnswer(longAnswer);
        
        assertEquals(longAnswer, response.getStandardAnswer());
    }

    @Test
    void should_handleDifferentQuestionTypes() {
        String[] questionTypes = {"选择题", "填空题", "简答题", "计算题", "判断题", "论述题"};
        
        for (String questionType : questionTypes) {
            QuestionResponse response = new QuestionResponse();
            response.setQuestionType(questionType);
            
            assertEquals(questionType, response.getQuestionType());
        }
    }

    @Test
    void should_handleSameCreatedAndUpdatedTime() {
        LocalDateTime now = LocalDateTime.now();
        QuestionResponse response = new QuestionResponse();
        
        response.setCreatedAt(now);
        response.setUpdatedAt(now);
        
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void should_handleDifferentCreatedAndUpdatedTime() {
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        QuestionResponse response = new QuestionResponse();
        
        response.setCreatedAt(createdAt);
        response.setUpdatedAt(updatedAt);
        
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(updatedAt, response.getUpdatedAt());
        assertTrue(updatedAt.isAfter(createdAt));
    }

    @Test
    void should_handleSameIdAndHomeworkId() {
        UUID sameId = UUID.randomUUID();
        QuestionResponse response = new QuestionResponse();
        
        response.setId(sameId);
        response.setHomeworkId(sameId);
        
        assertEquals(sameId, response.getId());
        assertEquals(sameId, response.getHomeworkId());
    }

    @Test
    void should_handleDifferentIdAndHomeworkId() {
        UUID id = UUID.randomUUID();
        UUID homeworkId = UUID.randomUUID();
        QuestionResponse response = new QuestionResponse();
        
        response.setId(id);
        response.setHomeworkId(homeworkId);
        
        assertEquals(id, response.getId());
        assertEquals(homeworkId, response.getHomeworkId());
        assertNotEquals(id, homeworkId);
    }

    @Test
    void should_toStringWithNullValues() {
        QuestionResponse response = new QuestionResponse();

        String toString = response.toString();

        assertTrue(toString.contains("QuestionResponse{"));
        assertTrue(toString.contains("id=null"));
        assertTrue(toString.contains("content='null'"));
        assertTrue(toString.contains("questionType='null'"));
        assertTrue(toString.contains("standardAnswer='null'"));
        assertTrue(toString.contains("orderIndex=null"));
        assertTrue(toString.contains("homeworkId=null"));
        assertTrue(toString.contains("createdAt=null"));
        assertTrue(toString.contains("updatedAt=null"));
    }
}