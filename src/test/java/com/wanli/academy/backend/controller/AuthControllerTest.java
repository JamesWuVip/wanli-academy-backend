package com.wanli.academy.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.academy.backend.dto.AuthResponse;
import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RefreshTokenRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AuthController测试类
 * 测试认证相关的API端点
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@AutoConfigureWebMvc
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("应该返回200状态码当健康检查时")
    void should_returnOkStatus_whenHealthCheck() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/health", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("认证服务运行正常");
    }

    @Test
    @DisplayName("应该返回201状态码当注册数据有效时")
    void should_returnCreatedStatus_whenValidRegistrationData() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("测试");
        registerRequest.setLastName("用户");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("test-access-token");
        authResponse.setRefreshToken("test-refresh-token");
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(3600L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("注册成功");
    }

    @Test
    @DisplayName("应该返回200状态码当登录凭据有效时")
    void should_returnOkStatus_whenValidLoginCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("test-access-token");
        authResponse.setRefreshToken("test-refresh-token");
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(3600L);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("登录成功");
    }

    @Test
    @DisplayName("应该返回200状态码当刷新令牌有效时")
    void should_returnOkStatus_whenValidRefreshToken() throws Exception {
        // Given
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("new-access-token");
        authResponse.setRefreshToken("new-refresh-token");
        authResponse.setTokenType("Bearer");
        authResponse.setExpiresIn(3600L);

        when(authService.refreshToken(anyString())).thenReturn(authResponse);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RefreshTokenRequest> request = new HttpEntity<>(refreshRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("令牌刷新成功");
    }

    @Test
    @DisplayName("应该返回200状态码当检查用户名可用性时")
    void should_returnOkStatus_whenCheckingUsernameAvailability() throws Exception {
        // Given
        when(authService.isUsernameAvailable(anyString())).thenReturn(true);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/check-username?username=testuser", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("用户名可用");
    }

    @Test
    @DisplayName("应该返回200状态码当检查邮箱可用性时")
    void should_returnOkStatus_whenCheckingEmailAvailability() throws Exception {
        // Given
        when(authService.isEmailAvailable(anyString())).thenReturn(true);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/check-email?email=test@example.com", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("邮箱可用");
    }

    @Test
    @DisplayName("应该返回400状态码当注册数据无效时")
    void should_returnBadRequest_whenInvalidRegistrationData() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        // 故意留空必填字段

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("请求参数验证失败");
    }

    @Test
    @DisplayName("应该返回400状态码当注册失败时")
    void should_returnBadRequest_whenRegistrationFails() throws Exception {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("测试");
        registerRequest.setLastName("用户");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("用户名已存在"));

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/register", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("用户名已存在");
    }

    @Test
    @DisplayName("应该返回400状态码当登录数据无效时")
    void should_returnBadRequest_whenInvalidLoginData() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        // 故意留空必填字段

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("请求参数验证失败");
    }

    @Test
    @DisplayName("应该返回401状态码当登录凭据无效时")
    void should_returnUnauthorized_whenInvalidLoginCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("用户名或密码错误"));

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("用户名或密码错误");
    }

    @Test
    @DisplayName("应该返回400状态码当刷新令牌数据无效时")
    void should_returnBadRequest_whenInvalidRefreshTokenData() throws Exception {
        // Given
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        // 故意留空必填字段

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RefreshTokenRequest> request = new HttpEntity<>(refreshRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("请求参数验证失败");
    }

    @Test
    @DisplayName("应该返回401状态码当刷新令牌无效时")
    void should_returnUnauthorized_whenInvalidRefreshToken() throws Exception {
        // Given
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        when(authService.refreshToken(anyString()))
                .thenThrow(new RuntimeException("刷新令牌无效或已过期"));

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RefreshTokenRequest> request = new HttpEntity<>(refreshRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/refresh", request, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("刷新令牌无效或已过期");
    }

    @Test
    @DisplayName("应该返回400状态码当用户名参数为空时")
    void should_returnBadRequest_whenUsernameParameterIsEmpty() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/check-username?username=", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("用户名不能为空");
    }

    @Test
    @DisplayName("应该返回200状态码当用户名不可用时")
    void should_returnOkStatus_whenUsernameNotAvailable() throws Exception {
        // Given
        when(authService.isUsernameAvailable(anyString())).thenReturn(false);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/check-username?username=existinguser", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("用户名已存在");
    }

    @Test
    @DisplayName("应该返回400状态码当邮箱参数为空时")
    void should_returnBadRequest_whenEmailParameterIsEmpty() throws Exception {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/check-email?email=", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("邮箱不能为空");
    }

    @Test
    @DisplayName("应该返回200状态码当邮箱不可用时")
    void should_returnOkStatus_whenEmailNotAvailable() throws Exception {
        // Given
        when(authService.isEmailAvailable(anyString())).thenReturn(false);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/auth/check-email?email=existing@example.com", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("邮箱已存在");
    }
}