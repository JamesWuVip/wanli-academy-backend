package com.wanli.academy.backend.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("错误响应DTO测试")
class ErrorResponseTest {

    @Test
    @DisplayName("应该正确创建空的错误响应")
    void should_createEmptyErrorResponse() {
        // When
        ErrorResponse errorResponse = new ErrorResponse();

        // Then
        assertNull(errorResponse.getCode());
        assertNull(errorResponse.getMessage());
        assertEquals(0L, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("应该正确创建带参数的错误响应")
    void should_createErrorResponseWithParameters() {
        // Given
        String code = "TEST_ERROR";
        String message = "测试错误消息";
        long timestamp = System.currentTimeMillis();

        // When
        ErrorResponse errorResponse = new ErrorResponse(code, message, timestamp);

        // Then
        assertEquals(code, errorResponse.getCode());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(timestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("应该正确设置和获取code")
    void should_setAndGetCode() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String code = "VALIDATION_ERROR";

        // When
        errorResponse.setCode(code);

        // Then
        assertEquals(code, errorResponse.getCode());
    }

    @Test
    @DisplayName("应该正确设置和获取message")
    void should_setAndGetMessage() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String message = "参数验证失败";

        // When
        errorResponse.setMessage(message);

        // Then
        assertEquals(message, errorResponse.getMessage());
    }

    @Test
    @DisplayName("应该正确设置和获取timestamp")
    void should_setAndGetTimestamp() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        long timestamp = System.currentTimeMillis();

        // When
        errorResponse.setTimestamp(timestamp);

        // Then
        assertEquals(timestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("应该处理null值的code")
    void should_handleNullCode() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();

        // When
        errorResponse.setCode(null);

        // Then
        assertNull(errorResponse.getCode());
    }

    @Test
    @DisplayName("应该处理null值的message")
    void should_handleNullMessage() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();

        // When
        errorResponse.setMessage(null);

        // Then
        assertNull(errorResponse.getMessage());
    }

    @Test
    @DisplayName("应该处理空字符串的code")
    void should_handleEmptyCode() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String emptyCode = "";

        // When
        errorResponse.setCode(emptyCode);

        // Then
        assertEquals(emptyCode, errorResponse.getCode());
    }

    @Test
    @DisplayName("应该处理空字符串的message")
    void should_handleEmptyMessage() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String emptyMessage = "";

        // When
        errorResponse.setMessage(emptyMessage);

        // Then
        assertEquals(emptyMessage, errorResponse.getMessage());
    }

    @Test
    @DisplayName("应该处理负数timestamp")
    void should_handleNegativeTimestamp() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        long negativeTimestamp = -1L;

        // When
        errorResponse.setTimestamp(negativeTimestamp);

        // Then
        assertEquals(negativeTimestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("应该处理零值timestamp")
    void should_handleZeroTimestamp() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        long zeroTimestamp = 0L;

        // When
        errorResponse.setTimestamp(zeroTimestamp);

        // Then
        assertEquals(zeroTimestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("应该正确实现toString方法")
    void should_implementToStringCorrectly() {
        // Given
        String code = "TEST_ERROR";
        String message = "测试错误";
        long timestamp = 1234567890L;
        ErrorResponse errorResponse = new ErrorResponse(code, message, timestamp);

        // When
        String result = errorResponse.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("ErrorResponse{"));
        assertTrue(result.contains("code='" + code + "'"));
        assertTrue(result.contains("message='" + message + "'"));
        assertTrue(result.contains("timestamp=" + timestamp));
    }

    @Test
    @DisplayName("应该正确处理toString中的null值")
    void should_handleNullValuesInToString() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(null, null, 0L);

        // When
        String result = errorResponse.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("ErrorResponse{"));
        assertTrue(result.contains("code='null'"));
        assertTrue(result.contains("message='null'"));
        assertTrue(result.contains("timestamp=0"));
    }

    @Test
    @DisplayName("应该正确处理长字符串")
    void should_handleLongStrings() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String longCode = "A".repeat(1000);
        String longMessage = "B".repeat(1000);

        // When
        errorResponse.setCode(longCode);
        errorResponse.setMessage(longMessage);

        // Then
        assertEquals(longCode, errorResponse.getCode());
        assertEquals(longMessage, errorResponse.getMessage());
    }

    @Test
    @DisplayName("应该正确处理特殊字符")
    void should_handleSpecialCharacters() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        String specialCode = "ERROR_测试_123!@#$%^&*()";
        String specialMessage = "错误消息包含特殊字符: \n\t\r\"'";

        // When
        errorResponse.setCode(specialCode);
        errorResponse.setMessage(specialMessage);

        // Then
        assertEquals(specialCode, errorResponse.getCode());
        assertEquals(specialMessage, errorResponse.getMessage());
    }

    @Test
    @DisplayName("应该正确处理最大timestamp值")
    void should_handleMaxTimestamp() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        long maxTimestamp = Long.MAX_VALUE;

        // When
        errorResponse.setTimestamp(maxTimestamp);

        // Then
        assertEquals(maxTimestamp, errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("应该正确处理最小timestamp值")
    void should_handleMinTimestamp() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        long minTimestamp = Long.MIN_VALUE;

        // When
        errorResponse.setTimestamp(minTimestamp);

        // Then
        assertEquals(minTimestamp, errorResponse.getTimestamp());
    }
}