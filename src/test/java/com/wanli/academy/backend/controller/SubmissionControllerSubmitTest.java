package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.controller.SubmissionController.SubmitAssignmentRequest;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SubmissionController提交操作测试类
 * 测试学生提交作业相关的API端点
 */
@WebMvcTest(value = SubmissionController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("提交控制器-提交操作测试")
class SubmissionControllerSubmitTest extends BaseControllerTest {

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
        
        lenient().when(permissionService.isTeacher()).thenReturn(false);
        lenient().when(permissionService.isAdmin()).thenReturn(false);
    }

    @Test
    @DisplayName("学生提交作业成功")
    void should_submitAssignment_when_validDataProvided() throws Exception {
        // Given
        SubmitAssignmentRequest request = new SubmitAssignmentRequest();
        request.setContent("学生提交的内容");
        request.setFilePath("/path/to/file");
        
        when(submissionService.submitAssignment(eq(testAssignmentId), eq("学生提交的内容"), eq("/path/to/file")))
            .thenReturn(submissionResponse);

        // When & Then
        mockMvc.perform(post("/api/assignments/{assignmentId}/submissions", testAssignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$.assignmentId").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$.content").value("学生提交的内容"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        verify(submissionService).submitAssignment(testAssignmentId, "学生提交的内容", "/path/to/file");
    }

    @Test
    @DisplayName("学生提交作业时作业不存在应返回404")
    void should_returnNotFound_when_submittingToNonExistentAssignment() throws Exception {
        // Given
        SubmitAssignmentRequest request = new SubmitAssignmentRequest();
        request.setContent("学生提交的内容");
        
        when(submissionService.submitAssignment(eq(testAssignmentId), eq("学生提交的内容"), isNull()))
            .thenThrow(new IllegalArgumentException("作业不存在"));

        // When & Then
        mockMvc.perform(post("/api/assignments/{assignmentId}/submissions", testAssignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(submissionService).submitAssignment(testAssignmentId, "学生提交的内容", null);
    }

    @Test
    @DisplayName("学生重复提交作业应返回409")
    void should_returnConflict_when_submittingDuplicateAssignment() throws Exception {
        // Given
        SubmitAssignmentRequest request = new SubmitAssignmentRequest();
        request.setContent("学生提交的内容");
        
        when(submissionService.submitAssignment(eq(testAssignmentId), eq("学生提交的内容"), isNull()))
            .thenThrow(new IllegalStateException("已经提交过该作业"));

        // When & Then
        mockMvc.perform(post("/api/assignments/{assignmentId}/submissions", testAssignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(submissionService).submitAssignment(testAssignmentId, "学生提交的内容", null);
    }

    @Test
    @DisplayName("学生更新提交成功")
    void should_updateSubmission_when_validDataProvided() throws Exception {
        // Given
        SubmitAssignmentRequest request = new SubmitAssignmentRequest();
        request.setContent("更新后的内容");
        request.setFilePath("/path/to/updated/file");
        
        SubmissionResponse updatedResponse = new SubmissionResponse();
        updatedResponse.setId(testSubmissionId);
        updatedResponse.setAssignmentId(testAssignmentId);
        updatedResponse.setAssignmentTitle("测试作业");
        updatedResponse.setStudentId(3L);
        updatedResponse.setStudentUsername("student");
        updatedResponse.setContent("更新后的内容");
        updatedResponse.setStatus("SUBMITTED");
        updatedResponse.setSubmittedAt(LocalDateTime.now());
        
        when(submissionService.updateSubmission(eq(testSubmissionId), eq("更新后的内容"), eq("/path/to/updated/file")))
            .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSubmissionId.toString()))
                .andExpect(jsonPath("$.content").value("更新后的内容"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        verify(submissionService).updateSubmission(testSubmissionId, "更新后的内容", "/path/to/updated/file");
    }

    @Test
    @DisplayName("更新提交时提交不存在应返回404")
    void should_returnNotFound_when_updatingNonExistentSubmission() throws Exception {
        // Given
        SubmitAssignmentRequest request = new SubmitAssignmentRequest();
        request.setContent("更新后的内容");
        
        when(submissionService.updateSubmission(eq(testSubmissionId), eq("更新后的内容"), isNull()))
            .thenThrow(new IllegalArgumentException("提交记录不存在"));

        // When & Then
        mockMvc.perform(put("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(submissionService).updateSubmission(testSubmissionId, "更新后的内容", null);
    }

    @Test
    @DisplayName("更新已批改的提交应返回400")
    void should_returnBadRequest_when_updatingGradedSubmission() throws Exception {
        // Given
        SubmitAssignmentRequest request = new SubmitAssignmentRequest();
        request.setContent("更新后的内容");
        
        when(submissionService.updateSubmission(eq(testSubmissionId), eq("更新后的内容"), isNull()))
            .thenThrow(new IllegalStateException("已批改的提交无法修改"));

        // When & Then
        mockMvc.perform(put("/api/submissions/{submissionId}", testSubmissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(submissionService).updateSubmission(testSubmissionId, "更新后的内容", null);
    }
}