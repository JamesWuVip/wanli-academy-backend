package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.controller.SubmissionController.GradeSubmissionRequest;
import com.wanli.academy.backend.controller.SubmissionController.SubmitAssignmentRequest;
import com.wanli.academy.backend.service.SubmissionService;
import com.wanli.academy.backend.service.PermissionService;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Optional;



import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MvcResult;
import static org.hamcrest.Matchers.*;

/**
 * 提交控制器测试 - 基础测试
 * 其他测试已拆分到专门的测试文件中：
 * - SubmissionControllerSubmitTest.java - 提交操作相关测试
 * - SubmissionControllerGradingTest.java - 批改操作相关测试
 * - SubmissionControllerQueryTest.java - 查询操作相关测试
 */
@WebMvcTest(value = SubmissionController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("提交控制器测试")
class SubmissionControllerTest extends BaseControllerTest {

    @MockBean
    private SubmissionService submissionService;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private com.wanli.academy.backend.repository.UserRepository userRepository;
    
    @MockBean
    private com.wanli.academy.backend.repository.AssignmentRepository assignmentRepository;
    
    @MockBean
    private com.wanli.academy.backend.repository.SubmissionRepository submissionRepository;

    private SubmissionResponse submissionResponse;
    private UUID testSubmissionId;
    private UUID testAssignmentId;
    
    // Helper method to convert object to JSON
    public String toJson(Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }

    @BeforeEach
    void setUp() {
        testSubmissionId = UUID.randomUUID();
        testAssignmentId = UUID.randomUUID();
        
        submissionResponse = new SubmissionResponse();
        submissionResponse.setId(testSubmissionId);
        submissionResponse.setAssignmentId(testAssignmentId);
        submissionResponse.setStudentId(1L);
        submissionResponse.setContent("测试提交内容");
        submissionResponse.setStatus("SUBMITTED");
        submissionResponse.setSubmittedAt(LocalDateTime.now());
        
        // 默认权限设置
        lenient().when(permissionService.isTeacher()).thenReturn(true);
        lenient().when(permissionService.isStudent()).thenReturn(false);
        lenient().when(permissionService.getCurrentUserId()).thenReturn(1L);
        lenient().when(permissionService.getCurrentUsername()).thenReturn("teacher");
        lenient().when(permissionService.canAccessSubmission(any(UUID.class))).thenReturn(true);
        lenient().when(permissionService.canGradeSubmission(any(UUID.class))).thenReturn(true);
        lenient().when(permissionService.canAccessAssignment(any(UUID.class))).thenReturn(true);
        
        // 默认服务方法设置
        lenient().when(submissionService.getSubmissionById(testSubmissionId)).thenReturn(submissionResponse);
    }

    // 基础控制器访问测试
    @Test
    public void should_accessController_when_simpleRequest() throws Exception {
        // Given
        when(submissionService.getPendingGradeSubmissions()).thenReturn(Collections.emptyList());
        
        // When & Then
        mockMvc.perform(get("/api/submissions/pending-grade"))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    @Test
    public void should_gradeSubmission_when_noPreAuthorizeRequired() throws Exception {
        // Given
        GradeSubmissionRequest gradeRequest = new GradeSubmissionRequest();
        gradeRequest.setScore(85);
        gradeRequest.setFeedback("做得很好！");
        
        submissionResponse.setScore(85);
        submissionResponse.setFeedback("做得很好！");
        submissionResponse.setStatus("GRADED");
        
        when(submissionService.gradeSubmission(eq(testSubmissionId), eq(85), eq("做得很好！")));
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/submissions/{submissionId}/grade", testSubmissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(gradeRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("Response status: " + result.getResponse().getStatus());
        System.out.println("Response body: " + result.getResponse().getContentAsString());
    }
    
    // 批改相关测试已迁移到 SubmissionControllerGradingTest.java
    // 获取待批改提交列表相关测试已迁移到 SubmissionControllerGradingTest.java
    // 学生更新提交相关测试已迁移到 SubmissionControllerSubmitTest.java
    // 学生提交作业相关测试已迁移到 SubmissionControllerSubmitTest.java
    // 查询相关测试已迁移到 SubmissionControllerQueryTest.java
    // 异常处理相关测试已迁移到对应的专门测试文件中
}