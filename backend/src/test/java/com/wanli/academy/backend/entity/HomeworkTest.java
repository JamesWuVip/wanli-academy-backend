package com.wanli.academy.backend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HomeworkTest {

    private Homework homework;
    private Long testCreatorId;

    @BeforeEach
    void setUp() {
        homework = new Homework();
        testCreatorId = 123L;
    }

    @Test
    void should_createHomeworkWithDefaultConstructor() {
        Homework newHomework = new Homework();
        
        assertNotNull(newHomework);
        assertNull(newHomework.getId());
        assertNull(newHomework.getTitle());
        assertNull(newHomework.getDescription());
        assertNull(newHomework.getCreatorId());
        assertNull(newHomework.getCreatedAt());
        assertNull(newHomework.getUpdatedAt());
        assertNull(newHomework.getCreator());
        assertNotNull(newHomework.getQuestions());
        assertTrue(newHomework.getQuestions().isEmpty());
    }

    @Test
    void should_createHomeworkWithParameterizedConstructor() {
        String title = "Math Homework";
        String description = "Algebra problems";
        
        Homework newHomework = new Homework(title, description, testCreatorId);
        
        assertNotNull(newHomework);
        assertEquals(title, newHomework.getTitle());
        assertEquals(description, newHomework.getDescription());
        assertEquals(testCreatorId, newHomework.getCreatorId());
        assertNull(newHomework.getId());
        assertNull(newHomework.getCreatedAt());
        assertNull(newHomework.getUpdatedAt());
        assertNull(newHomework.getCreator());
        assertNotNull(newHomework.getQuestions());
        assertTrue(newHomework.getQuestions().isEmpty());
    }

    @Test
    void should_setAndGetId() {
        UUID testId = UUID.randomUUID();
        homework.setId(testId);
        
        assertEquals(testId, homework.getId());
    }

    @Test
    void should_setAndGetTitle() {
        String title = "Science Homework";
        homework.setTitle(title);
        
        assertEquals(title, homework.getTitle());
    }

    @Test
    void should_handleNullTitle() {
        homework.setTitle(null);
        
        assertNull(homework.getTitle());
    }

    @Test
    void should_handleEmptyTitle() {
        homework.setTitle("");
        
        assertEquals("", homework.getTitle());
    }

    @Test
    void should_handleLongTitle() {
        String longTitle = "a".repeat(255);
        homework.setTitle(longTitle);
        
        assertEquals(longTitle, homework.getTitle());
        assertEquals(255, homework.getTitle().length());
    }

    @Test
    void should_setAndGetDescription() {
        String description = "This is a detailed description of the homework assignment.";
        homework.setDescription(description);
        
        assertEquals(description, homework.getDescription());
    }

    @Test
    void should_handleNullDescription() {
        homework.setDescription(null);
        
        assertNull(homework.getDescription());
    }

    @Test
    void should_handleEmptyDescription() {
        homework.setDescription("");
        
        assertEquals("", homework.getDescription());
    }

    @Test
    void should_handleLongDescription() {
        String longDescription = "a".repeat(1000);
        homework.setDescription(longDescription);
        
        assertEquals(longDescription, homework.getDescription());
        assertEquals(1000, homework.getDescription().length());
    }

    @Test
    void should_setAndGetCreatorId() {
        homework.setCreatorId(testCreatorId);
        
        assertEquals(testCreatorId, homework.getCreatorId());
    }

    @Test
    void should_handleNullCreatorId() {
        homework.setCreatorId(null);
        
        assertNull(homework.getCreatorId());
    }

    @Test
    void should_setAndGetCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        homework.setCreatedAt(now);
        
        assertEquals(now, homework.getCreatedAt());
    }

    @Test
    void should_setAndGetUpdatedAt() {
        LocalDateTime now = LocalDateTime.now();
        homework.setUpdatedAt(now);
        
        assertEquals(now, homework.getUpdatedAt());
    }

    @Test
    void should_setAndGetCreator() {
        User creator = new User();
        homework.setCreator(creator);
        
        assertEquals(creator, homework.getCreator());
    }

    @Test
    void should_handleNullCreator() {
        homework.setCreator(null);
        
        assertNull(homework.getCreator());
    }

    @Test
    void should_setAndGetQuestions() {
        List<Question> questions = new ArrayList<>();
        Question question1 = new Question();
        Question question2 = new Question();
        questions.add(question1);
        questions.add(question2);
        
        homework.setQuestions(questions);
        
        assertEquals(questions, homework.getQuestions());
        assertEquals(2, homework.getQuestions().size());
        assertTrue(homework.getQuestions().contains(question1));
        assertTrue(homework.getQuestions().contains(question2));
    }

    @Test
    void should_handleNullQuestions() {
        homework.setQuestions(null);
        
        assertNull(homework.getQuestions());
    }

    @Test
    void should_handleEmptyQuestions() {
        List<Question> emptyQuestions = new ArrayList<>();
        homework.setQuestions(emptyQuestions);
        
        assertEquals(emptyQuestions, homework.getQuestions());
        assertTrue(homework.getQuestions().isEmpty());
    }

    @Test
    void should_addQuestionSuccessfully() {
        Question question = new Question();
        
        homework.addQuestion(question);
        
        assertEquals(1, homework.getQuestions().size());
        assertTrue(homework.getQuestions().contains(question));
        assertEquals(homework, question.getHomework());
    }

    @Test
    void should_addMultipleQuestions() {
        Question question1 = new Question();
        Question question2 = new Question();
        Question question3 = new Question();
        
        homework.addQuestion(question1);
        homework.addQuestion(question2);
        homework.addQuestion(question3);
        
        assertEquals(3, homework.getQuestions().size());
        assertTrue(homework.getQuestions().contains(question1));
        assertTrue(homework.getQuestions().contains(question2));
        assertTrue(homework.getQuestions().contains(question3));
        assertEquals(homework, question1.getHomework());
        assertEquals(homework, question2.getHomework());
        assertEquals(homework, question3.getHomework());
    }

    @Test
    void should_removeQuestionSuccessfully() {
        Question question = new Question();
        homework.addQuestion(question);
        
        homework.removeQuestion(question);
        
        assertEquals(0, homework.getQuestions().size());
        assertFalse(homework.getQuestions().contains(question));
        assertNull(question.getHomework());
    }

    @Test
    void should_removeSpecificQuestionFromMultiple() {
        Question question1 = new Question();
        Question question2 = new Question();
        Question question3 = new Question();
        
        homework.addQuestion(question1);
        homework.addQuestion(question2);
        homework.addQuestion(question3);
        
        homework.removeQuestion(question2);
        
        assertEquals(2, homework.getQuestions().size());
        assertTrue(homework.getQuestions().contains(question1));
        assertFalse(homework.getQuestions().contains(question2));
        assertTrue(homework.getQuestions().contains(question3));
        assertEquals(homework, question1.getHomework());
        assertNull(question2.getHomework());
        assertEquals(homework, question3.getHomework());
    }

    @Test
    void should_handleRemoveNonExistentQuestion() {
        Question existingQuestion = new Question();
        Question nonExistentQuestion = new Question();
        
        homework.addQuestion(existingQuestion);
        
        // This should not throw an exception
        homework.removeQuestion(nonExistentQuestion);
        
        assertEquals(1, homework.getQuestions().size());
        assertTrue(homework.getQuestions().contains(existingQuestion));
        assertNull(nonExistentQuestion.getHomework());
    }

    @Test
    void should_maintainBidirectionalRelationshipWhenAddingQuestion() {
        Question question = new Question();
        
        homework.addQuestion(question);
        
        // Check bidirectional relationship
        assertTrue(homework.getQuestions().contains(question));
        assertEquals(homework, question.getHomework());
    }

    @Test
    void should_maintainBidirectionalRelationshipWhenRemovingQuestion() {
        Question question = new Question();
        homework.addQuestion(question);
        
        homework.removeQuestion(question);
        
        // Check bidirectional relationship is properly broken
        assertFalse(homework.getQuestions().contains(question));
        assertNull(question.getHomework());
    }

    @Test
    void should_returnCorrectToString() {
        UUID testId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 1, 2, 11, 0);
        
        homework.setId(testId);
        homework.setTitle("Test Homework");
        homework.setDescription("Test Description");
        homework.setCreatorId(testCreatorId);
        homework.setCreatedAt(createdAt);
        homework.setUpdatedAt(updatedAt);
        
        String result = homework.toString();
        
        assertTrue(result.contains("Homework{"));
        assertTrue(result.contains("id=" + testId));
        assertTrue(result.contains("title='Test Homework'"));
        assertTrue(result.contains("description='Test Description'"));
        assertTrue(result.contains("creatorId=" + testCreatorId));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("updatedAt=" + updatedAt));
    }

    @Test
    void should_returnCorrectToStringWithNullValues() {
        String result = homework.toString();
        
        assertTrue(result.contains("Homework{"));
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("title='null'"));
        assertTrue(result.contains("description='null'"));
        assertTrue(result.contains("creatorId=null"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("updatedAt=null"));
    }

    @Test
    void should_beEqualWhenSameObject() {
        assertTrue(homework.equals(homework));
    }

    @Test
    void should_beEqualWhenSameId() {
        UUID testId = UUID.randomUUID();
        Homework homework1 = new Homework();
        Homework homework2 = new Homework();
        
        homework1.setId(testId);
        homework2.setId(testId);
        
        assertTrue(homework1.equals(homework2));
        assertTrue(homework2.equals(homework1));
    }

    @Test
    void should_notBeEqualWhenDifferentId() {
        Homework homework1 = new Homework();
        Homework homework2 = new Homework();
        
        homework1.setId(UUID.randomUUID());
        homework2.setId(UUID.randomUUID());
        
        assertFalse(homework1.equals(homework2));
        assertFalse(homework2.equals(homework1));
    }

    @Test
    void should_notBeEqualWhenOneIdIsNull() {
        Homework homework1 = new Homework();
        Homework homework2 = new Homework();
        
        homework1.setId(UUID.randomUUID());
        homework2.setId(null);
        
        assertFalse(homework1.equals(homework2));
        assertFalse(homework2.equals(homework1));
    }

    @Test
    void should_notBeEqualWhenBothIdsAreNull() {
        Homework homework1 = new Homework();
        Homework homework2 = new Homework();
        
        assertFalse(homework1.equals(homework2));
    }

    @Test
    void should_notBeEqualWhenComparedWithNull() {
        assertFalse(homework.equals(null));
    }

    @Test
    void should_notBeEqualWhenComparedWithDifferentClass() {
        assertFalse(homework.equals("not a homework"));
        assertFalse(homework.equals(new Object()));
    }

    @Test
    void should_haveSameHashCodeForSameClass() {
        Homework homework1 = new Homework();
        Homework homework2 = new Homework();
        
        assertEquals(homework1.hashCode(), homework2.hashCode());
    }

    @Test
    void should_haveSameHashCodeRegardlessOfId() {
        Homework homework1 = new Homework();
        Homework homework2 = new Homework();
        
        homework1.setId(UUID.randomUUID());
        homework2.setId(UUID.randomUUID());
        
        assertEquals(homework1.hashCode(), homework2.hashCode());
    }

    @Test
    void should_handleCompleteHomeworkWorkflow() {
        // Create homework
        homework.setTitle("Complete Math Assignment");
        homework.setDescription("Solve all algebra problems");
        homework.setCreatorId(testCreatorId);
        
        // Add questions
        Question q1 = new Question();
        Question q2 = new Question();
        Question q3 = new Question();
        
        homework.addQuestion(q1);
        homework.addQuestion(q2);
        homework.addQuestion(q3);
        
        // Verify state
        assertEquals("Complete Math Assignment", homework.getTitle());
        assertEquals("Solve all algebra problems", homework.getDescription());
        assertEquals(testCreatorId, homework.getCreatorId());
        assertEquals(3, homework.getQuestions().size());
        
        // Remove one question
        homework.removeQuestion(q2);
        
        assertEquals(2, homework.getQuestions().size());
        assertTrue(homework.getQuestions().contains(q1));
        assertFalse(homework.getQuestions().contains(q2));
        assertTrue(homework.getQuestions().contains(q3));
    }

    @Test
    void should_handleCreatorRelationship() {
        User creator = new User();
        creator.setId(1L);
        creator.setUsername("teacher1");
        
        homework.setCreator(creator);
        homework.setCreatorId(1L);
        
        assertEquals(creator, homework.getCreator());
        assertEquals(Long.valueOf(1L), homework.getCreatorId());
    }
}