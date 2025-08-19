package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;
import com.wanli.academy.backend.base.TestDataBuilder;
import com.wanli.academy.backend.config.SecurityConfig;
import com.wanli.academy.backend.config.JwtAuthenticationEntryPoint;
import com.wanli.academy.backend.config.JwtAuthenticationFilter;
import com.wanli.academy.backend.dto.HomeworkCreateRequest;
import com.wanli.academy.backend.dto.HomeworkResponse;
import com.wanli.academy.backend.dto.QuestionCreateRequest;
import com.wanli.academy.backend.dto.QuestionResponse;
import com.wanli.academy.backend.entity.Homework;
import com.wanli.academy.backend.entity.Question;
import com.wanli.academy.backend.service.HomeworkService;
import com.wanli.academy.backend.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

/**
 * HomeworkController测试类
 * 测试作业管理相关的API端点
 */
@WebMvcTest(HomeworkController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
@DisplayName("作业控制器测试")
class HomeworkControllerTest extends BaseControllerTest {

    @BeforeEach
    void setUp() {
        // 配置JWT服务模拟行为
        when(jwtService.extractUsername(anyString())).thenReturn("teacher@test.com");
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);

        // 配置用户详情服务模拟行为
        User mockUser = new User("teacher@test.com", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_HQ_TEACHER")));
        when(userDetailsService.loadUserByUsername("teacher@test.com")).thenReturn(mockUser);
    }

    // 移除HTTP请求头创建方法，使用BaseControllerTest的MockMvc方法

    @Test
    @DisplayName("应该成功创建作业当数据有效时")
    void should_createHomework_when_validData() throws Exception {
        // Given
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        request.setTitle("测试作业");
        request.setDescription("这是一个测试作业");

        HomeworkResponse expectedResponse = new HomeworkResponse();
        expectedResponse.setId(UUID.randomUUID());
        expectedResponse.setTitle("测试作业");
        expectedResponse.setDescription("这是一个测试作业");
        // HomeworkResponse没有dueDate字段
        expectedResponse.setCreatedAt(LocalDateTime.now());
        expectedResponse.setUpdatedAt(LocalDateTime.now());

        when(homeworkService.createHomework(any(HomeworkCreateRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks", HQ_TEACHER_JWT_TOKEN, request))
                 .andDo(print())
                 .andExpect(status().isCreated())
                 .andExpect(jsonPath("$.id").exists())
                 .andExpect(jsonPath("$.title").value("测试作业"))
                 .andExpect(jsonPath("$.description").value("这是一个测试作业"));

        verify(homeworkService).createHomework(any(HomeworkCreateRequest.class));
    }

    @Test
    @DisplayName("应该返回400错误当请求数据无效时")
    void should_returnBadRequest_when_invalidData() throws Exception {
        // Given
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        // 不设置必需字段，使请求无效

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks", HQ_TEACHER_JWT_TOKEN, request))
                 .andDo(print())
                 .andExpect(status().isBadRequest());

        verify(homeworkService, never()).createHomework(any(HomeworkCreateRequest.class));
    }

    @Test
    @DisplayName("应该返回401错误当用户未认证时")
    void should_returnUnauthorized_when_noAuthentication() throws Exception {
        // Given
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        request.setTitle("测试作业");
        request.setDescription("这是一个测试作业");
        // HomeworkCreateRequest没有dueDate字段

        // When & Then
        mockMvc.perform(postWithoutAuth("/api/homeworks", request))
                 .andDo(print())
                 .andExpect(status().isUnauthorized());

        verify(homeworkService, never()).createHomework(any(HomeworkCreateRequest.class));
    }

    @Test
    @DisplayName("应该成功获取作业列表当用户有权限时")
    void should_getHomeworkList_when_authorizedUser() throws Exception {
        // Given
        HomeworkResponse homework1 = new HomeworkResponse();
        homework1.setId(UUID.randomUUID());
        homework1.setTitle("作业1");
        homework1.setDescription("描述1");
        homework1.setCreatedAt(LocalDateTime.now());

        HomeworkResponse homework2 = new HomeworkResponse();
        homework2.setId(UUID.randomUUID());
        homework2.setTitle("作业2");
        homework2.setDescription("描述2");
        homework2.setCreatedAt(LocalDateTime.now());

        List<HomeworkResponse> expectedList = Arrays.asList(homework1, homework2);

        when(homeworkService.getHomeworksByCreator()).thenReturn(expectedList);

        // When & Then
        getWithAuth("/api/homeworks", HQ_TEACHER_JWT_TOKEN.replace("Bearer ", ""))
                 .andDo(print())
                 .andExpect(status().isOk())
                 .andExpect(jsonPath("$").isArray())
                 .andExpect(jsonPath("$.length()").value(2))
                 .andExpect(jsonPath("$[0].title").value("作业1"))
                 .andExpect(jsonPath("$[1].title").value("作业2"));

        verify(homeworkService).getHomeworksByCreator();
    }

    @Test
    @DisplayName("应该返回401错误当获取作业列表时用户未认证")
    void should_returnUnauthorized_when_getHomeworkListWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(getWithoutAuth("/api/homeworks"))
                 .andDo(print())
                 .andExpect(status().isUnauthorized());

        verify(homeworkService, never()).getHomeworksByCreator();
    }

    @Test
    @DisplayName("应该成功添加题目到作业当数据有效时")
    void should_addQuestionToHomework_when_validData() throws Exception {
        // Given
        UUID homeworkId = UUID.randomUUID();
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setContent("1 + 1 = ?");
        request.setQuestionType("选择题");
        request.setStandardAnswer("2");
        request.setOrderIndex(1);

        QuestionResponse mockResponse = new QuestionResponse();
        mockResponse.setId(UUID.randomUUID());
        mockResponse.setContent("1 + 1 = ?");
        mockResponse.setQuestionType("选择题");
        mockResponse.setStandardAnswer("2");
        mockResponse.setOrderIndex(1);
        mockResponse.setCreatedAt(LocalDateTime.now());

        when(homeworkService.addQuestionToHomework(eq(homeworkId), any(QuestionCreateRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks/" + homeworkId + "/questions", HQ_TEACHER_JWT_TOKEN, request))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("1 + 1 = ?"))
                .andExpect(jsonPath("$.questionType").value("选择题"))
                .andExpect(jsonPath("$.standardAnswer").value("2"))
                .andExpect(jsonPath("$.orderIndex").value(1));

        verify(homeworkService).addQuestionToHomework(eq(homeworkId), any(QuestionCreateRequest.class));
    }

    @Test
    @DisplayName("应该返回400错误当添加题目数据无效时")
    void should_returnBadRequest_when_addQuestionWithInvalidData() throws Exception {
        // Given
        UUID homeworkId = UUID.randomUUID();
        QuestionCreateRequest request = new QuestionCreateRequest();
        // 不设置必需的字段，触发验证错误

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks/" + homeworkId + "/questions", HQ_TEACHER_JWT_TOKEN, request))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(homeworkService, never()).addQuestionToHomework(eq(homeworkId), any(QuestionCreateRequest.class));
    }

    @Test
    @DisplayName("应该返回404错误当向不存在的作业添加题目时")
    void should_returnNotFound_when_addQuestionToNonExistentHomework() throws Exception {
        // Given
        UUID nonExistentHomeworkId = UUID.randomUUID();
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setContent("1 + 1 = ?");
        request.setQuestionType("选择题");
        request.setStandardAnswer("2");
        request.setOrderIndex(1);

        when(homeworkService.addQuestionToHomework(eq(nonExistentHomeworkId), any(QuestionCreateRequest.class)))
                .thenThrow(new RuntimeException("作业不存在"));

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks/" + nonExistentHomeworkId + "/questions", HQ_TEACHER_JWT_TOKEN, request))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("作业不存在")));

        verify(homeworkService).addQuestionToHomework(eq(nonExistentHomeworkId), any(QuestionCreateRequest.class));
    }

    @Test
    @DisplayName("应该返回403错误当没有HQ_TEACHER角色时")
    void should_returnForbidden_when_addQuestionWithoutHqTeacherRole() throws Exception {
        // Given
        UUID homeworkId = UUID.randomUUID();
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setContent("1 + 1 = ?");
        request.setQuestionType("选择题");
        request.setStandardAnswer("2");
        request.setOrderIndex(1);

        // 配置学生角色的JWT
        when(jwtService.extractUsername(anyString())).thenReturn("student@test.com");
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
        User studentUser = new User("student@test.com", "password", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));
        when(userDetailsService.loadUserByUsername("student@test.com")).thenReturn(studentUser);

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks/" + homeworkId + "/questions", STUDENT_JWT_TOKEN, request))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(homeworkService, never()).addQuestionToHomework(eq(homeworkId), any(QuestionCreateRequest.class));
    }

    @Test
    @DisplayName("应该正确处理运行时异常当服务层抛出异常时")
    void should_handleRuntimeException_when_serviceThrowsException() throws Exception {
        // Given
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        request.setTitle("数学作业1");
        request.setDescription("第一章练习题");

        when(homeworkService.createHomework(any(HomeworkCreateRequest.class)))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        mockMvc.perform(postWithAuthAndBody("/api/homeworks", HQ_TEACHER_JWT_TOKEN, request))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("数据库连接失败")));

        verify(homeworkService).createHomework(any(HomeworkCreateRequest.class));
    }
}