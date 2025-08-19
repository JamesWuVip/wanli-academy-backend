package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.controller.SubmissionController.GradeSubmissionRequest;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SubmissionController批改操作测试类
 * 测试教师批改提交相关的API端点
 */
@WebMvcTest(value = SubmissionController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("提交控制器-批改操作测试")
class SubmissionControllerGradingTest extends BaseControllerTest {

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
        submissionResponse.setStatus("GRADED");
        submissionResponse.setScore(85);
        submissionResponse.setFeedback("做得很好！");
        submissionResponse.setSubmittedAt(LocalDateTime.now());
        submissionResponse.setGradedAt(LocalDateTime.now());
        
        lenient().when(permissionService.isTeacher()).thenReturn(true);
        lenient().when(permissionService.isAdmin()).thenReturn(false);
    }

    @Test
    public void should_gradeSubmission_when_noPreAuthorizeRequired() throws Exception {
        // Given
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setScore(85);
        request.setFeedback("做得很好！");
        
        when(submissionService.gradeSubmission(eq(testSubmissionId), eq(85), eq("做得很好！")))
            .thenReturn(submissionResponse);

        // When & Then
        mockMvc.perform(post("/api/submissions/{submissionId}/grade", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.feedback").value("做得很好！"))
                .andExpect(jsonPath("$.status").value("GRADED"));

        verify(submissionService).gradeSubmission(testSubmissionId, 85, "做得很好！");
    }

    @Test
    @DisplayName("批改提交成功")
    void should_gradeSubmission_when_validDataProvided() throws Exception {
        // Given
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setScore(85);
        request.setFeedback("做得很好！");
        
        when(submissionService.gradeSubmission(eq(testSubmissionId), eq(85), eq("做得很好！")))
            .thenReturn(submissionResponse);

        // When & Then
        mockMvc.perform(post("/api/submissions/{submissionId}/grade", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$.score").value(85))
                .andExpect(jsonPath("$.feedback").value("做得很好！"))
                .andExpect(jsonPath("$.status").value("GRADED"));

        verify(submissionService).gradeSubmission(testSubmissionId, 85, "做得很好！");
    }

    @Test
    @DisplayName("获取待批改提交列表成功")
    void should_getPendingGradeSubmissions_when_requestIsValid() throws Exception {
        // Given
        List<SubmissionResponse> pendingSubmissions = Arrays.asList(submissionResponse);
        when(submissionService.getPendingGradeSubmissions()).thenReturn(pendingSubmissions);

        // When & Then
        mockMvc.perform(get("/api/submissions/pending-grade")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$[0].assignmentTitle").value("测试作业"));

        verify(submissionService).getPendingGradeSubmissions();
    }

    @Test
    @DisplayName("无权限批改提交应返回400")
    void should_returnBadRequest_when_gradingSubmissionWithoutPermission() throws Exception {
        // Given
        when(permissionService.isTeacher()).thenReturn(false);
        
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setScore(85);
        request.setFeedback("做得很好！");
        
        when(submissionService.gradeSubmission(eq(testSubmissionId), eq(85), eq("做得很好！")))
            .thenThrow(new RuntimeException("无权限批改此提交"));

        // When & Then
        mockMvc.perform(post("/api/submissions/{submissionId}/grade", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(submissionService).gradeSubmission(testSubmissionId, 85, "做得很好！");
    }

    @Test
    @DisplayName("批改提交时提交不存在应返回404")
    void should_returnNotFound_when_gradingNonExistentSubmission() throws Exception {
        // Given
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setScore(85);
        request.setFeedback("做得很好！");
        
        when(submissionService.gradeSubmission(eq(testSubmissionId), eq(85), eq("做得很好！")))
            .thenThrow(new IllegalArgumentException("提交记录不存在"));

        // When & Then
        mockMvc.perform(post("/api/submissions/{submissionId}/grade", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(submissionService).gradeSubmission(testSubmissionId, 85, "做得很好！");
    }

    @Test
    @DisplayName("批改提交时分数无效应返回400")
    void should_returnBadRequest_when_gradingWithInvalidScore() throws Exception {
        // Given
        GradeSubmissionRequest request = new GradeSubmissionRequest();
        request.setScore(-10); // 无效分数
        request.setFeedback("分数无效");
        
        when(submissionService.gradeSubmission(eq(testSubmissionId), eq(-10), eq("分数无效")))
            .thenThrow(new IllegalArgumentException("分数必须在0到100之间"));

        // When & Then
        mockMvc.perform(post("/api/submissions/{submissionId}/grade", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}