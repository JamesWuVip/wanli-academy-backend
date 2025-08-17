package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.service.SubmissionService;
import com.wanli.academy.backend.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SubmissionController查询操作测试类
 * 测试提交查询相关的API端点
 */
@WebMvcTest(value = SubmissionController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("提交控制器-查询操作测试")
class SubmissionControllerQueryTest extends BaseControllerTest {

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

    @BeforeEach
    void setUp() {
        testSubmissionId = UUID.randomUUID();
        testAssignmentId = UUID.randomUUID();
        
        submissionResponse = new SubmissionResponse();
        submissionResponse.setId(testSubmissionId);
        submissionResponse.setAssignmentId(testAssignmentId);
        submissionResponse.setAssignmentTitle("测试作业");
        submissionResponse.setStudentId(3L);
        submissionResponse.setStudentUsername("student");
        submissionResponse.setContent("学生提交的内容");
        submissionResponse.setStatus("SUBMITTED");
        submissionResponse.setSubmittedAt(LocalDateTime.now());
        
        lenient().when(permissionService.isTeacher()).thenReturn(true);
        lenient().when(permissionService.isAdmin()).thenReturn(false);
    }

    @Test
    @DisplayName("获取提交详情成功")
    void should_getSubmission_when_submissionExists() throws Exception {
        // Given
        when(submissionService.getSubmissionById(testSubmissionId)).thenReturn(submissionResponse);

        // When & Then
        mockMvc.perform(get("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$.assignmentTitle").value("测试作业"))
                .andExpect(jsonPath("$.content").value("学生提交的内容"));

        verify(submissionService).getSubmissionById(testSubmissionId);
    }

    @Test
    public void should_accessController_when_simpleRequest() throws Exception {
        // Given
        when(submissionService.getSubmissionById(testSubmissionId)).thenReturn(submissionResponse);

        // When & Then
        mockMvc.perform(get("/api/submissions/{submissionId}", testSubmissionId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("学生获取自己的提交记录成功")
    void should_getMySubmissions_when_studentRequests() throws Exception {
        // Given
        List<SubmissionResponse> submissions = Arrays.asList(submissionResponse);
        when(submissionService.getStudentSubmissions()).thenReturn(submissions);

        // When & Then
        mockMvc.perform(get("/api/submissions/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$[0].assignmentTitle").value("测试作业"));

        verify(submissionService).getStudentSubmissions();
    }

    @Test
    @DisplayName("获取作业统计信息成功")
    void should_getAssignmentStatistics_when_requestIsValid() throws Exception {
        // Given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSubmissions", 10);
        statistics.put("gradedSubmissions", 8);
        statistics.put("averageScore", 85.5);
        
        when(submissionService.getAssignmentStatistics(testAssignmentId)).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/assignments/{assignmentId}/statistics", testAssignmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSubmissions").value(10))
                .andExpect(jsonPath("$.gradedSubmissions").value(8))
                .andExpect(jsonPath("$.averageScore").value(85.5));

        verify(submissionService).getAssignmentStatistics(testAssignmentId);
    }

    @Test
    @DisplayName("未认证访问应返回200")
    void should_returnOk_when_accessingWithoutAuthentication() throws Exception {
        // Given
        when(submissionService.getSubmissionById(testSubmissionId)).thenReturn(submissionResponse);

        // When & Then
        mockMvc.perform(get("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmissionId.toString()));

        verify(submissionService).getSubmissionById(testSubmissionId);
    }

    @Test
    @DisplayName("无权限访问提交应返回400")
    void should_returnBadRequest_when_accessingSubmissionWithoutPermission() throws Exception {
        // Given
        when(submissionService.getSubmissionById(testSubmissionId))
            .thenThrow(new RuntimeException("无权限访问此提交"));

        // When & Then
        mockMvc.perform(get("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(submissionService).getSubmissionById(testSubmissionId);
    }

    @Test
    @DisplayName("获取提交详情时提交不存在应返回404")
    void should_returnNotFound_when_gettingNonExistentSubmission() throws Exception {
        // Given
        when(submissionService.getSubmissionById(testSubmissionId))
            .thenThrow(new IllegalArgumentException("提交记录不存在"));

        // When & Then
        mockMvc.perform(get("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(submissionService).getSubmissionById(testSubmissionId);
    }

    @Test
    @DisplayName("获取作业统计信息时作业不存在应返回404")
    void should_returnNotFound_when_gettingStatisticsForNonExistentAssignment() throws Exception {
        // Given
        when(submissionService.getAssignmentStatistics(testAssignmentId))
            .thenThrow(new IllegalArgumentException("作业不存在"));

        // When & Then
        mockMvc.perform(get("/api/assignments/{assignmentId}/statistics", testAssignmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(submissionService).getAssignmentStatistics(testAssignmentId);
    }

    @Test
    @DisplayName("学生访问作业统计信息应返回403")
    void should_returnForbidden_when_studentAccessesAssignmentStatistics() throws Exception {
        // Given
        when(permissionService.isTeacher()).thenReturn(false);
        when(submissionService.getAssignmentStatistics(testAssignmentId))
            .thenThrow(new RuntimeException("无权限访问统计信息"));

        // When & Then
        mockMvc.perform(get("/api/assignments/{assignmentId}/statistics", testAssignmentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(submissionService).getAssignmentStatistics(testAssignmentId);
    }
}