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
 * RefreshTokenRequest DTO 测试类
 */
@DisplayName("RefreshTokenRequest DTO 测试")
class RefreshTokenRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("默认构造函数应该创建空对象")
    void should_createEmptyObject_when_usingDefaultConstructor() {
        // Given & When
        RefreshTokenRequest request = new RefreshTokenRequest();

        // Then
        assertNull(request.getRefreshToken());
    }

    @Test
    @DisplayName("带参构造函数应该正确设置refreshToken字段")
    void should_setRefreshTokenField_when_usingParameterizedConstructor() {
        // Given
        String refreshToken = "refresh-token-123456";

        // When
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        // Then
        assertEquals(refreshToken, request.getRefreshToken());
    }

    @Test
    @DisplayName("Getter和Setter方法应该正常工作")
    void should_workCorrectly_when_usingGettersAndSetters() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        String refreshToken = "new-refresh-token-789";

        // When
        request.setRefreshToken(refreshToken);

        // Then
        assertEquals(refreshToken, request.getRefreshToken());
    }

    @Test
    @DisplayName("toString方法应该保护敏感token信息")
    void should_protectSensitiveToken_when_callingToString() {
        // Given
        String refreshToken = "secret-refresh-token-abcdef";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        // When
        String result = request.toString();

        // Then
        assertTrue(result.contains("RefreshTokenRequest{"));
        assertTrue(result.contains("refreshToken='[PROTECTED]'"));
        assertFalse(result.contains(refreshToken), "Actual refresh token should not appear in toString");
    }

    @Test
    @DisplayName("有效refreshToken应该通过验证")
    void should_passValidation_when_refreshTokenIsValid() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("空refreshToken应该验证失败")
    void should_failValidation_when_refreshTokenIsBlank() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("");

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("刷新令牌不能为空")));
    }

    @Test
    @DisplayName("null refreshToken应该验证失败")
    void should_failValidation_when_refreshTokenIsNull() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(null);

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("刷新令牌不能为空")));
    }

    @Test
    @DisplayName("只包含空格的refreshToken应该验证失败")
    void should_failValidation_when_refreshTokenIsOnlyWhitespace() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("   ");

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("刷新令牌不能为空")));
    }

    @Test
    @DisplayName("应该正确处理各种有效的token格式")
    void should_handleVariousValidTokenFormats_correctly() {
        // Given
        String[] validTokens = {
            "simple-token",
            "token.with.dots",
            "token_with_underscores",
            "token-with-dashes",
            "TokenWithMixedCase123",
            "very-long-token-with-many-characters-and-numbers-123456789",
            "jwt.eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
        };

        for (String token : validTokens) {
            // When
            RefreshTokenRequest request = new RefreshTokenRequest(token);
            Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

            // Then
            assertTrue(violations.isEmpty(), "Token " + token + " should be valid");
            assertEquals(token, request.getRefreshToken());
        }
    }

    @Test
    @DisplayName("toString方法应该能处理null token")
    void should_handleNullToken_when_callingToString() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(null);

        // When & Then
        assertDoesNotThrow(() -> {
            String result = request.toString();
            assertTrue(result.contains("RefreshTokenRequest{"));
            assertTrue(result.contains("refreshToken='[PROTECTED]'"));
        });
    }

    @Test
    @DisplayName("应该支持token的重新设置")
    void should_supportTokenReset_correctly() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("initial-token");
        String newToken = "new-token";

        // When
        request.setRefreshToken(newToken);

        // Then
        assertEquals(newToken, request.getRefreshToken());
        
        // When - 设置为null
        request.setRefreshToken(null);
        
        // Then
        assertNull(request.getRefreshToken());
    }

    @Test
    @DisplayName("验证错误信息应该准确")
    void should_provideAccurateErrorMessage_when_validationFails() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("");

        // When
        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<RefreshTokenRequest> violation = violations.iterator().next();
        assertEquals("刷新令牌不能为空", violation.getMessage());
        assertEquals("refreshToken", violation.getPropertyPath().toString());
    }
}