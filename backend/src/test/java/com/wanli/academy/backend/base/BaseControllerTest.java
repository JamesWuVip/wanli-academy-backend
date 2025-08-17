package com.wanli.academy.backend.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;

import com.wanli.academy.backend.service.HomeworkService;
import com.wanli.academy.backend.service.AuthService;

import com.wanli.academy.backend.service.CustomUserDetailsService;
import com.wanli.academy.backend.service.JwtService;

/**
 * Controller测试基类
 * 提供通用的测试配置和工具方法，遵循DRY原则
 */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Mock所有Service层依赖
    @MockBean
    protected AuthService authService;

    @MockBean
    protected HomeworkService homeworkService;
    


    @MockBean
    protected CustomUserDetailsService userDetailsService;

    @MockBean
    protected JwtService jwtService;

    // 测试用户常量
    protected static final String TEST_USERNAME = "testuser";
    protected static final String TEST_EMAIL = "test@wanli.com";
    protected static final String TEST_PASSWORD = "password123";
    protected static final String TEST_FULL_NAME = "Test User";
    protected static final Long TEST_USER_ID = 1L;

    // JWT Token常量
    protected static final String VALID_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.test";
    protected static final String ADMIN_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNTE2MjM5MDIyfQ.admin";
    protected static final String HQ_TEACHER_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwicm9sZSI6IkhRX1RFQUNIRVIiLCJpYXQiOjE1MTYyMzkwMjJ9.hqteacher";
    protected static final String STUDENT_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwicm9sZSI6IlNUVURFTlQiLCJpYXQiOjE1MTYyMzkwMjJ9.student";

    @BeforeEach
    void setUp() {
        // 子类可以重写此方法进行特定的初始化
    }

    // ==================== HTTP请求构建工具方法 ====================

    /**
     * 创建带认证头的GET请求
     */
    protected ResultActions getWithAuth(String url, String jwtToken) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON));
    }
    
    protected ResultActions getWithAuth(String url) throws Exception {
        return getWithAuth(url, HQ_TEACHER_JWT_TOKEN.replace("Bearer ", ""));
    }
    
    protected ResultActions getWithAdminAuth(String url) throws Exception {
        return getWithAuth(url, ADMIN_JWT_TOKEN.replace("Bearer ", ""));
    }

    /**
     * 创建带认证头的POST请求
     */
    protected MockHttpServletRequestBuilder postWithAuth(String url, String token) {
        return MockMvcRequestBuilders.post(url)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 创建带认证头和请求体的POST请求
     */
    protected MockHttpServletRequestBuilder postWithAuthAndBody(String url, String token, Object requestBody) throws Exception {
        return MockMvcRequestBuilders.post(url)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));
    }

    /**
     * 创建带认证头的PUT请求
     */
    protected MockHttpServletRequestBuilder putWithAuth(String url, String token) {
        return MockMvcRequestBuilders.put(url)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 创建带认证头和请求体的PUT请求
     */
    protected MockHttpServletRequestBuilder putWithAuthAndBody(String url, String token, Object requestBody) throws Exception {
        return MockMvcRequestBuilders.put(url)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));
    }

    /**
     * 创建带认证头的DELETE请求
     */
    protected MockHttpServletRequestBuilder deleteWithAuth(String url, String token) {
        return MockMvcRequestBuilders.delete(url)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 创建不带认证的POST请求
     */
    protected MockHttpServletRequestBuilder postWithoutAuth(String url, Object requestBody) throws Exception {
        return MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody));
    }

    /**
     * 创建不带认证的GET请求
     */
    protected MockHttpServletRequestBuilder getWithoutAuth(String url) {
        return MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON);
    }

    // ==================== JSON转换工具方法 ====================

    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * 将JSON字符串转换为对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}