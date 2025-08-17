package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssignmentCreateRequest DTO 测试类
 */
@DisplayName("AssignmentCreateRequest DTO 测试")
class AssignmentCreateRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("默认构造函数应该创建空对象")
    void should_createEmptyObject_when_usingDefaultConstructor() {
        // Given & When
        AssignmentCreateRequest request = new AssignmentCreateRequest();

        // Then
        assertNull(request.getTitle());
        assertNull(request.getDescription());
        assertNull(request.getDueDate());
        assertNull(request.getTotalScore());
        assertNull(request.getStatus());
    }

    @Test
    @DisplayName("带参构造函数应该正确设置所有字段")
    void should_setAllFields_when_usingParameterizedConstructor() {
        // Given
        String title = "数学作业";
        String description = "完成第一章练习";
        LocalDateTime dueDate = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        Integer totalScore = 100;
        String status = "DRAFT";

        // When
        AssignmentCreateRequest request = new AssignmentCreateRequest(title, description, dueDate, totalScore, status);

        // Then
        assertEquals(title, request.getTitle());
        assertEquals(description, request.getDescription());
        assertEquals(dueDate, request.getDueDate());
        assertEquals(totalScore, request.getTotalScore());
        assertEquals(status, request.getStatus());
    }

    @Test
    @DisplayName("Getter和Setter方法应该正常工作")
    void should_workCorrectly_when_usingGettersAndSetters() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest();
        String title = "物理作业";
        String description = "完成力学部分";
        LocalDateTime dueDate = LocalDateTime.of(2024, 11, 30, 18, 0, 0);
        Integer totalScore = 80;
        String status = "PUBLISHED";

        // When
        request.setTitle(title);
        request.setDescription(description);
        request.setDueDate(dueDate);
        request.setTotalScore(totalScore);
        request.setStatus(status);

        // Then
        assertEquals(title, request.getTitle());
        assertEquals(description, request.getDescription());
        assertEquals(dueDate, request.getDueDate());
        assertEquals(totalScore, request.getTotalScore());
        assertEquals(status, request.getStatus());
    }

    @Test
    @DisplayName("toString方法应该返回正确格式的字符串")
    void should_returnCorrectFormat_when_callingToString() {
        // Given
        String title = "化学作业";
        String description = "有机化学实验";
        LocalDateTime dueDate = LocalDateTime.of(2024, 10, 15, 12, 0, 0);
        Integer totalScore = 90;
        String status = "CLOSED";
        
        AssignmentCreateRequest request = new AssignmentCreateRequest(title, description, dueDate, totalScore, status);

        // When
        String result = request.toString();

        // Then
        assertTrue(result.contains("AssignmentCreateRequest{"));
        assertTrue(result.contains("title='" + title + "'"));
        assertTrue(result.contains("description='" + description + "'"));
        assertTrue(result.contains("dueDate=" + dueDate));
        assertTrue(result.contains("totalScore=" + totalScore));
        assertTrue(result.contains("status='" + status + "'"));
    }

    @Test
    @DisplayName("有效数据应该通过验证")
    void should_passValidation_when_dataIsValid() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "数学作业第一章",
            "完成所有练习题",
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            100,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("空标题应该验证失败")
    void should_failValidation_when_titleIsBlank() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "",
            "描述",
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            100,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业标题不能为空")));
    }

    @Test
    @DisplayName("标题过长应该验证失败")
    void should_failValidation_when_titleIsTooLong() {
        // Given
        String longTitle = "a".repeat(201);
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            longTitle,
            "描述",
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            100,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业标题长度必须在1-200个字符之间")));
    }

    @Test
    @DisplayName("描述过长应该验证失败")
    void should_failValidation_when_descriptionIsTooLong() {
        // Given
        String longDescription = "a".repeat(2001);
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "标题",
            longDescription,
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            100,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业描述长度不能超过2000个字符")));
    }

    @Test
    @DisplayName("空截止日期应该验证失败")
    void should_failValidation_when_dueDateIsNull() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "标题",
            "描述",
            null,
            100,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("截止日期不能为空")));
    }

    @Test
    @DisplayName("负数总分应该验证失败")
    void should_failValidation_when_totalScoreIsNegative() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "标题",
            "描述",
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            -1,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("总分不能小于0")));
    }

    @Test
    @DisplayName("总分超过最大值应该验证失败")
    void should_failValidation_when_totalScoreExceedsMaximum() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "标题",
            "描述",
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            1001,
            "DRAFT"
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("总分不能超过1000")));
    }

    @Test
    @DisplayName("空状态应该验证失败")
    void should_failValidation_when_statusIsBlank() {
        // Given
        AssignmentCreateRequest request = new AssignmentCreateRequest(
            "标题",
            "描述",
            LocalDateTime.of(2024, 12, 31, 23, 59, 59),
            100,
            ""
        );

        // When
        Set<ConstraintViolation<AssignmentCreateRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("作业状态不能为空")));
    }
}