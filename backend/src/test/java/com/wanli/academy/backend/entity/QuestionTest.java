package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {

    private Question question;
    private UUID testHomeworkId;
    private Map<String, Object> testContent;
    private Map<String, Object> testStandardAnswer;

    @BeforeEach
    void setUp() {
        question = new Question();
        testHomeworkId = UUID.randomUUID();
        
        testContent = new HashMap<>();
        testContent.put("type", "multiple_choice");
        testContent.put("question", "What is 2+2?");
        testContent.put("options", new String[]{"3", "4", "5", "6"});
        
        testStandardAnswer = new HashMap<>();
        testStandardAnswer.put("answer", "4");
        testStandardAnswer.put("explanation", "2+2 equals 4");
    }

    @Test
    void should_createQuestionWithDefaultConstructor() {
        Question newQuestion = new Question();
        
        assertNotNull(newQuestion);
        assertNull(newQuestion.getId());
        assertNull(newQuestion.getHomeworkId());
        assertNull(newQuestion.getQuestionType());
        assertNull(newQuestion.getContent());
        assertNull(newQuestion.getStandardAnswer());
        assertNull(newQuestion.getOrderIndex());
        assertNull(newQuestion.getCreatedAt());
        assertNull(newQuestion.getUpdatedAt());
        assertNull(newQuestion.getHomework());
    }

    @Test
    void should_createQuestionWithParameterizedConstructor() {
        Question newQuestion = new Question(testHomeworkId, "multiple_choice", testContent, 1);
        
        assertNotNull(newQuestion);
        assertEquals(testHomeworkId, newQuestion.getHomeworkId());
        assertEquals("multiple_choice", newQuestion.getQuestionType());
        assertEquals(testContent, newQuestion.getContent());
        assertEquals(1, newQuestion.getOrderIndex());
        assertNull(newQuestion.getId());
        assertNull(newQuestion.getStandardAnswer());
        assertNull(newQuestion.getCreatedAt());
        assertNull(newQuestion.getUpdatedAt());
    }

    @Test
    void should_setAndGetId() {
        UUID testId = UUID.randomUUID();
        question.setId(testId);
        
        assertEquals(testId, question.getId());
    }

    @Test
    void should_setAndGetHomeworkId() {
        question.setHomeworkId(testHomeworkId);
        
        assertEquals(testHomeworkId, question.getHomeworkId());
    }

    @Test
    void should_setAndGetQuestionType() {
        String questionType = "essay";
        question.setQuestionType(questionType);
        
        assertEquals(questionType, question.getQuestionType());
    }

    @Test
    void should_handleNullQuestionType() {
        question.setQuestionType(null);
        
        assertNull(question.getQuestionType());
    }

    @Test
    void should_handleEmptyQuestionType() {
        question.setQuestionType("");
        
        assertEquals("", question.getQuestionType());
    }

    @Test
    void should_setAndGetContent() {
        question.setContent(testContent);
        
        assertEquals(testContent, question.getContent());
        assertEquals("multiple_choice", question.getContent().get("type"));
        assertEquals("What is 2+2?", question.getContent().get("question"));
    }

    @Test
    void should_handleNullContent() {
        question.setContent(null);
        
        assertNull(question.getContent());
    }

    @Test
    void should_handleEmptyContent() {
        Map<String, Object> emptyContent = new HashMap<>();
        question.setContent(emptyContent);
        
        assertEquals(emptyContent, question.getContent());
        assertTrue(question.getContent().isEmpty());
    }

    @Test
    void should_setAndGetStandardAnswer() {
        question.setStandardAnswer(testStandardAnswer);
        
        assertEquals(testStandardAnswer, question.getStandardAnswer());
        assertEquals("4", question.getStandardAnswer().get("answer"));
        assertEquals("2+2 equals 4", question.getStandardAnswer().get("explanation"));
    }

    @Test
    void should_handleNullStandardAnswer() {
        question.setStandardAnswer(null);
        
        assertNull(question.getStandardAnswer());
    }

    @Test
    void should_handleEmptyStandardAnswer() {
        Map<String, Object> emptyAnswer = new HashMap<>();
        question.setStandardAnswer(emptyAnswer);
        
        assertEquals(emptyAnswer, question.getStandardAnswer());
        assertTrue(question.getStandardAnswer().isEmpty());
    }

    @Test
    void should_setAndGetOrderIndex() {
        Integer orderIndex = 5;
        question.setOrderIndex(orderIndex);
        
        assertEquals(orderIndex, question.getOrderIndex());
    }

    @Test
    void should_handleNullOrderIndex() {
        question.setOrderIndex(null);
        
        assertNull(question.getOrderIndex());
    }

    @Test
    void should_handleZeroOrderIndex() {
        question.setOrderIndex(0);
        
        assertEquals(0, question.getOrderIndex());
    }

    @Test
    void should_handleNegativeOrderIndex() {
        question.setOrderIndex(-1);
        
        assertEquals(-1, question.getOrderIndex());
    }

    @Test
    void should_setAndGetCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        question.setCreatedAt(now);
        
        assertEquals(now, question.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        question.setUpdatedAt(now);
        
        assertEquals(now, question.getUpdatedAt());
    }

    @Test
    void should_setAndGetHomework() {
        Homework homework = new Homework();
        question.setHomework(homework);
        
        assertEquals(homework, question.getHomework());
    }

    @Test
    void should_handleNullHomework() {
        question.setHomework(null);
        
        assertNull(question.getHomework());
    }

    @Test
    void should_returnCorrectToString() {
        UUID testId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0);
        
        question.setId(testId);
        question.setHomeworkId(testHomeworkId);
        question.setQuestionType("multiple_choice");
        question.setOrderIndex(1);
        question.setCreatedAt(createdAt);
        question.setUpdatedAt(updatedAt);
        
        String result = question.toString();
        
        assertTrue(result.contains("Question{"));
        assertTrue(result.contains("id=" + testId));
        assertTrue(result.contains("homeworkId=" + testHomeworkId));
        assertTrue(result.contains("questionType='multiple_choice'"));
        assertTrue(result.contains("orderIndex=1"));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("updatedAt=" + updatedAt));
    }

    @Test
    void should_returnCorrectToStringWithNullValues() {
        String result = question.toString();
        
        assertTrue(result.contains("Question{"));
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("homeworkId=null"));
        assertTrue(result.contains("questionType='null'"));
        assertTrue(result.contains("orderIndex=null"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("updatedAt=null"));
    }

    @Test
    void should_beEqualWhenSameObject() {
        assertTrue(question.equals(question));
    }

    @Test
    void should_beEqualWhenSameId() {
        UUID testId = UUID.randomUUID();
        Question question1 = new Question();
        Question question2 = new Question();
        
        question1.setId(testId);
        question2.setId(testId);
        
        assertTrue(question1.equals(question2));
        assertTrue(question2.equals(question1));
    }

    @Test
    void should_notBeEqualWhenDifferentId() {
        Question question1 = new Question();
        Question question2 = new Question();
        
        question1.setId(UUID.randomUUID());
        question2.setId(UUID.randomUUID());
        
        assertFalse(question1.equals(question2));
        assertFalse(question2.equals(question1));
    }

    @Test
    void should_notBeEqualWhenOneIdIsNull() {
        Question question1 = new Question();
        Question question2 = new Question();
        
        question1.setId(UUID.randomUUID());
        question2.setId(null);
        
        assertFalse(question1.equals(question2));
        assertFalse(question2.equals(question1));
    }

    @Test
    void should_notBeEqualWhenBothIdsAreNull() {
        Question question1 = new Question();
        Question question2 = new Question();
        
        assertFalse(question1.equals(question2));
    }

    @Test
    void should_notBeEqualWhenComparedWithNull() {
        assertFalse(question.equals(null));
    }

    @Test
    void should_notBeEqualWhenComparedWithDifferentClass() {
        assertFalse(question.equals("not a question"));
        assertFalse(question.equals(new Object()));
    }

    @Test
    void should_haveSameHashCodeForSameClass() {
        Question question1 = new Question();
        Question question2 = new Question();
        
        assertEquals(question1.hashCode(), question2.hashCode());
    }

    @Test
    void should_haveSameHashCodeRegardlessOfId() {
        Question question1 = new Question();
        Question question2 = new Question();
        
        question1.setId(UUID.randomUUID());
        question2.setId(UUID.randomUUID());
        
        assertEquals(question1.hashCode(), question2.hashCode());
    }

    @Test
    void should_handleComplexJsonContent() {
        Map<String, Object> complexContent = new HashMap<>();
        complexContent.put("type", "coding");
        complexContent.put("language", "java");
        complexContent.put("difficulty", 3);
        
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("timeLimit", 30);
        nestedMap.put("memoryLimit", "256MB");
        complexContent.put("constraints", nestedMap);
        
        question.setContent(complexContent);
        
        assertEquals(complexContent, question.getContent());
        assertEquals("coding", question.getContent().get("type"));
        assertEquals(3, question.getContent().get("difficulty"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> constraints = (Map<String, Object>) question.getContent().get("constraints");
        assertEquals(30, constraints.get("timeLimit"));
        assertEquals("256MB", constraints.get("memoryLimit"));
    }

    @Test
    void should_handleComplexJsonStandardAnswer() {
        Map<String, Object> complexAnswer = new HashMap<>();
        complexAnswer.put("correctCode", "public class Solution { ... }");
        complexAnswer.put("testCases", new String[]{"test1", "test2", "test3"});
        complexAnswer.put("expectedOutput", "[1, 2, 3]");
        complexAnswer.put("points", 10);
        
        question.setStandardAnswer(complexAnswer);
        
        assertEquals(complexAnswer, question.getStandardAnswer());
        assertEquals("public class Solution { ... }", question.getStandardAnswer().get("correctCode"));
        assertEquals(10, question.getStandardAnswer().get("points"));
        
        String[] testCases = (String[]) question.getStandardAnswer().get("testCases");
        assertEquals(3, testCases.length);
        assertEquals("test1", testCases[0]);
    }

    @Test
    void should_handleQuestionTypeConstraints() {
        // Test maximum length constraint (50 characters)
        String longQuestionType = "a".repeat(50);
        question.setQuestionType(longQuestionType);
        
        assertEquals(longQuestionType, question.getQuestionType());
        assertEquals(50, question.getQuestionType().length());
    }

    @Test
    void should_maintainOrderIndexForSorting() {
        Question q1 = new Question();
        Question q2 = new Question();
        Question q3 = new Question();
        
        q1.setOrderIndex(3);
        q2.setOrderIndex(1);
        q3.setOrderIndex(2);
        
        assertTrue(q2.getOrderIndex() < q3.getOrderIndex());
        assertTrue(q3.getOrderIndex() < q1.getOrderIndex());
    }
}