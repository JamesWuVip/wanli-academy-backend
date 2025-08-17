package com.wanli.academy.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_createInstanceWithDefaultConstructor() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        assertNotNull(request);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        String content = "计算 2 + 3 = ?";
        String questionType = "选择题";
        String standardAnswer = "5";
        Integer orderIndex = 1;

        QuestionCreateRequest request = new QuestionCreateRequest(content, questionType, standardAnswer, orderIndex);

        assertEquals(content, request.getContent());
        assertEquals(questionType, request.getQuestionType());
        assertEquals(standardAnswer, request.getStandardAnswer());
        assertEquals(orderIndex, request.getOrderIndex());
    }

    @Test
    void should_setAndGetContent() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        String content = "计算 2 + 3 = ?";
        
        request.setContent(content);
        
        assertEquals(content, request.getContent());
    }

    @Test
    void should_setAndGetQuestionType() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        String questionType = "选择题";
        
        request.setQuestionType(questionType);
        
        assertEquals(questionType, request.getQuestionType());
    }

    @Test
    void should_setAndGetStandardAnswer() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        String standardAnswer = "5";
        
        request.setStandardAnswer(standardAnswer);
        
        assertEquals(standardAnswer, request.getStandardAnswer());
    }

    @Test
    void should_setAndGetOrderIndex() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        Integer orderIndex = 1;
        
        request.setOrderIndex(orderIndex);
        
        assertEquals(orderIndex, request.getOrderIndex());
    }

    @Test
    void should_returnCorrectToString() {
        String content = "计算 2 + 3 = ?";
        String questionType = "选择题";
        String standardAnswer = "5";
        Integer orderIndex = 1;
        QuestionCreateRequest request = new QuestionCreateRequest(content, questionType, standardAnswer, orderIndex);

        String toString = request.toString();

        assertTrue(toString.contains("QuestionCreateRequest{"));
        assertTrue(toString.contains("content='" + content + "'"));
        assertTrue(toString.contains("questionType='" + questionType + "'"));
        assertTrue(toString.contains("standardAnswer='" + standardAnswer + "'"));
        assertTrue(toString.contains("orderIndex=" + orderIndex));
    }

    @Test
    void should_passValidation_whenAllFieldsValid() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_failValidation_whenContentIsNull() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                null,
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目内容不能为空")));
    }

    @Test
    void should_failValidation_whenContentIsEmpty() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "",
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目内容不能为空")));
    }

    @Test
    void should_failValidation_whenContentIsBlank() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "   ",
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目内容不能为空")));
    }

    @Test
    void should_failValidation_whenContentTooLong() {
        String longContent = "a".repeat(2001); // 2001 characters
        QuestionCreateRequest request = new QuestionCreateRequest(
                longContent,
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目内容长度必须在1-2000个字符之间")));
    }

    @Test
    void should_passValidation_whenContentAtMaxLength() {
        String maxContent = "a".repeat(2000); // 2000 characters
        QuestionCreateRequest request = new QuestionCreateRequest(
                maxContent,
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_failValidation_whenQuestionTypeIsNull() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                null,
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目类型不能为空")));
    }

    @Test
    void should_failValidation_whenQuestionTypeIsEmpty() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目类型不能为空")));
    }

    @Test
    void should_failValidation_whenQuestionTypeIsBlank() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "   ",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目类型不能为空")));
    }

    @Test
    void should_failValidation_whenQuestionTypeTooLong() {
        String longQuestionType = "a".repeat(51); // 51 characters
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                longQuestionType,
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目类型长度不能超过50个字符")));
    }

    @Test
    void should_passValidation_whenQuestionTypeAtMaxLength() {
        String maxQuestionType = "a".repeat(50); // 50 characters
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                maxQuestionType,
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_passValidation_whenStandardAnswerIsNull() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                null,
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_passValidation_whenStandardAnswerIsEmpty() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_failValidation_whenStandardAnswerTooLong() {
        String longStandardAnswer = "a".repeat(2001); // 2001 characters
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                longStandardAnswer,
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("标准答案长度不能超过2000个字符")));
    }

    @Test
    void should_passValidation_whenStandardAnswerAtMaxLength() {
        String maxStandardAnswer = "a".repeat(2000); // 2000 characters
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                maxStandardAnswer,
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_failValidation_whenOrderIndexIsNull() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "5",
                null
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目顺序不能为空")));
    }

    @Test
    void should_failValidation_whenOrderIndexIsZero() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "5",
                0
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目顺序必须大于0")));
    }

    @Test
    void should_failValidation_whenOrderIndexIsNegative() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "5",
                -1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("题目顺序必须大于0")));
    }

    @Test
    void should_passValidation_whenOrderIndexIsOne() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "5",
                1
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_passValidation_whenOrderIndexIsLarge() {
        QuestionCreateRequest request = new QuestionCreateRequest(
                "计算 2 + 3 = ?",
                "选择题",
                "5",
                1000
        );

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_handleNullValues() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        
        assertNull(request.getContent());
        assertNull(request.getQuestionType());
        assertNull(request.getStandardAnswer());
        assertNull(request.getOrderIndex());
    }

    @Test
    void should_handleEmptyStrings() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        
        request.setContent("");
        request.setQuestionType("");
        request.setStandardAnswer("");
        
        assertEquals("", request.getContent());
        assertEquals("", request.getQuestionType());
        assertEquals("", request.getStandardAnswer());
    }

    @Test
    void should_testAllQuestionTypes() {
        String[] questionTypes = {"选择题", "填空题", "简答题", "计算题"};
        
        for (String questionType : questionTypes) {
            QuestionCreateRequest request = new QuestionCreateRequest(
                    "测试题目",
                    questionType,
                    "测试答案",
                    1
            );
            
            Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Question type " + questionType + " should be valid");
        }
    }
}