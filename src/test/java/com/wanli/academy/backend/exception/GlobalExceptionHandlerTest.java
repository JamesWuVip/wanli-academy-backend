package com.wanli.academy.backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("全局异常处理器测试")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("应该处理运行时异常")
    void should_handleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("测试运行时异常");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("测试运行时异常", response.getBody().getMessage());
        assertEquals("RUNTIME_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理访问拒绝异常")
    void should_handleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("访问被拒绝");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("访问被拒绝"));
        assertEquals("ACCESS_DENIED", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理方法参数验证异常")
    void should_handleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "name", "不能为空");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(fieldError));

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValidException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("请求参数验证失败"));
        assertTrue(response.getBody().getMessage().contains("name: 不能为空"));
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理绑定异常")
    void should_handleBindException() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "格式不正确");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(fieldError));

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBindException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("数据绑定失败"));
        assertTrue(response.getBody().getMessage().contains("email: 格式不正确"));
        assertEquals("BIND_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理约束违反异常")
    void should_handleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("约束违反错误");
        violations.add(violation);
        
        ConstraintViolationException exception = new ConstraintViolationException("约束违反", violations);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolationException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("约束违反"));
        assertEquals("CONSTRAINT_VIOLATION", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理非法参数异常")
    void should_handleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("非法参数");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("非法参数", response.getBody().getMessage());
        assertEquals("ILLEGAL_ARGUMENT", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理空指针异常")
    void should_handleNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("空指针异常");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNullPointerException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("系统内部错误，请联系管理员", response.getBody().getMessage());
        assertEquals("NULL_POINTER", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理其他未捕获异常")
    void should_handleGenericException() {
        // Given
        Exception exception = new Exception("未知异常");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("系统内部错误，请稍后重试", response.getBody().getMessage());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理空消息的运行时异常")
    void should_handleRuntimeExceptionWithNullMessage() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        // RuntimeException with null message will have null getMessage()
        assertNull(response.getBody().getMessage());
        assertEquals("RUNTIME_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    @DisplayName("应该处理空消息的访问拒绝异常")
    void should_handleAccessDeniedExceptionWithNullMessage() {
        // Given
        AccessDeniedException exception = new AccessDeniedException(null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getMessage()); // 应该有默认消息
        assertEquals("ACCESS_DENIED", response.getBody().getCode());
        assertTrue(response.getBody().getTimestamp() > 0);
    }
}