package com.wanli.academy.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HomeworkCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_createInstanceWithDefaultConstructor() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        assertNotNull(request);
    }

    @Test
    void should_createInstanceWithAllArgsConstructor() {
        String title = "数学作业第一章";
        String description = "完成第一章的所有练习题，包括基础题和提高题";

        HomeworkCreateRequest request = new HomeworkCreateRequest(title, description);

        assertEquals(title, request.getTitle());
        assertEquals(description, request.getDescription());
    }

    @Test
    void should_setAndGetTitle() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        String title = "数学作业第一章";
        
        request.setTitle(title);
        
        assertEquals(title, request.getTitle());
    }

    @Test
    void should_setAndGetDescription() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        String description = "完成第一章的所有练习题，包括基础题和提高题";
        
        request.setDescription(description);
        
        assertEquals(description, request.getDescription());
    }

    @Test
    void should_returnCorrectToString() {
        String title = "数学作业第一章";
        String description = "完成第一章的所有练习题";
        HomeworkCreateRequest request = new HomeworkCreateRequest(title, description);

        String toString = request.toString();

        assertTrue(toString.contains("HomeworkCreateRequest{"));
        assertTrue(toString.contains("title='" + title + "'"));
        assertTrue(toString.contains("description='" + description + "'"));
    }

    @Test
    void should_passValidation_whenAllFieldsValid() {
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "数学作业第一章",
                "完成第一章的所有练习题，包括基础题和提高题"
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_failValidation_whenTitleIsNull() {
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                null,
                "完成第一章的所有练习题"
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业标题不能为空")));
    }

    @Test
    void should_failValidation_whenTitleIsEmpty() {
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "",
                "完成第一章的所有练习题"
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业标题不能为空")));
    }

    @Test
    void should_failValidation_whenTitleIsBlank() {
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "   ",
                "完成第一章的所有练习题"
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业标题不能为空")));
    }

    @Test
    void should_failValidation_whenTitleTooLong() {
        String longTitle = "a".repeat(201); // 201 characters
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                longTitle,
                "完成第一章的所有练习题"
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业标题长度必须在1-200个字符之间")));
    }

    @Test
    void should_passValidation_whenTitleAtMaxLength() {
        String maxTitle = "a".repeat(200); // 200 characters
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                maxTitle,
                "完成第一章的所有练习题"
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_passValidation_whenDescriptionIsNull() {
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "数学作业第一章",
                null
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_passValidation_whenDescriptionIsEmpty() {
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "数学作业第一章",
                ""
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_failValidation_whenDescriptionTooLong() {
        String longDescription = "a".repeat(1001); // 1001 characters
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "数学作业第一章",
                longDescription
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业描述长度不能超过1000个字符")));
    }

    @Test
    void should_passValidation_whenDescriptionAtMaxLength() {
        String maxDescription = "a".repeat(1000); // 1000 characters
        HomeworkCreateRequest request = new HomeworkCreateRequest(
                "数学作业第一章",
                maxDescription
        );

        Set<ConstraintViolation<HomeworkCreateRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_handleNullValues() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        
        assertNull(request.getTitle());
        assertNull(request.getDescription());
    }

    @Test
    void should_handleEmptyStrings() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        
        request.setTitle("");
        request.setDescription("");
        
        assertEquals("", request.getTitle());
        assertEquals("", request.getDescription());
    }
}