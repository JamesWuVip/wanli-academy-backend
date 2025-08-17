package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginRequest DTO 测试类
 */
@DisplayName("LoginRequest DTO 测试")
class LoginRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("默认构造函数应该创建空对象")
    void should_createEmptyObject_when_usingDefaultConstructor() {
        // Given & When
        LoginRequest request = new LoginRequest();

        // Then
        assertNull(request.getUsernameOrEmail());
        assertNull(request.getPassword());
    }

    @Test
    @DisplayName("带参构造函数应该正确设置所有字段")
    void should_setAllFields_when_usingParameterizedConstructor() {
        // Given
        String usernameOrEmail = "test@example.com";
        String password = "password123";

        // When
        LoginRequest request = new LoginRequest(usernameOrEmail, password);

        // Then
        assertEquals(usernameOrEmail, request.getUsernameOrEmail());
        assertEquals(password, request.getPassword());
    }

    @Test
    @DisplayName("Getter和Setter方法应该正常工作")
    void should_workCorrectly_when_usingGettersAndSetters() {
        // Given
        LoginRequest request = new LoginRequest();
        String usernameOrEmail = "user123";
        String password = "newpassword";

        // When
        request.setUsernameOrEmail(usernameOrEmail);
        request.setPassword(password);

        // Then
        assertEquals(usernameOrEmail, request.getUsernameOrEmail());
        assertEquals(password, request.getPassword());
    }

    @Test
    @DisplayName("toString方法应该保护密码信息")
    void should_protectPassword_when_callingToString() {
        // Given
        String usernameOrEmail = "test@example.com";
        String password = "secretpassword";
        LoginRequest request = new LoginRequest(usernameOrEmail, password);

        // When
        String result = request.toString();

        // Then
        assertTrue(result.contains("LoginRequest{"));
        assertTrue(result.contains("usernameOrEmail='" + usernameOrEmail + "'"));
        assertTrue(result.contains("password='[PROTECTED]'"));
        assertFalse(result.contains(password)); // 确保实际密码不会出现在toString中
    }

    @Test
    @DisplayName("有效数据应该通过验证")
    void should_passValidation_when_dataIsValid() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("空用户名或邮箱应该验证失败")
    void should_failValidation_when_usernameOrEmailIsBlank() {
        // Given
        LoginRequest request = new LoginRequest("", "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名或邮箱不能为空")));
    }

    @Test
    @DisplayName("null用户名或邮箱应该验证失败")
    void should_failValidation_when_usernameOrEmailIsNull() {
        // Given
        LoginRequest request = new LoginRequest(null, "password123");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("用户名或邮箱不能为空")));
    }

    @Test
    @DisplayName("空密码应该验证失败")
    void should_failValidation_when_passwordIsBlank() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "");

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码不能为空")));
    }

    @Test
    @DisplayName("null密码应该验证失败")
    void should_failValidation_when_passwordIsNull() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", null);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码不能为空")));
    }

    @Test
    @DisplayName("密码过短应该验证失败")
    void should_failValidation_when_passwordIsTooShort() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "12345"); // 5个字符，少于最小长度6

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码长度必须在6-100个字符之间")));
    }

    @Test
    @DisplayName("密码过长应该验证失败")
    void should_failValidation_when_passwordIsTooLong() {
        // Given
        String longPassword = "a".repeat(101); // 101个字符，超过最大长度100
        LoginRequest request = new LoginRequest("test@example.com", longPassword);

        // When
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码长度必须在6-100个字符之间")));
    }

    @Test
    @DisplayName("边界值密码长度应该通过验证")
    void should_passValidation_when_passwordLengthIsBoundary() {
        // Given - 测试最小长度6
        LoginRequest request1 = new LoginRequest("test@example.com", "123456");
        // Given - 测试最大长度100
        String maxLengthPassword = "a".repeat(100);
        LoginRequest request2 = new LoginRequest("test@example.com", maxLengthPassword);

        // When
        Set<ConstraintViolation<LoginRequest>> violations1 = validator.validate(request1);
        Set<ConstraintViolation<LoginRequest>> violations2 = validator.validate(request2);

        // Then
        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
    }
}