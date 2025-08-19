package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;

import com.wanli.academy.backend.dto.AssignmentCreateRequest;
import com.wanli.academy.backend.service.AssignmentService;
import com.wanli.academy.backend.service.AssignmentServiceQuery;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.MockitoAnnotations;

/**
 * AssignmentController 异常处理测试类
 * 测试各种异常情况的处理
 */
@WebMvcTest(controllers = AssignmentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("作业控制器异常处理测试")
class AssignmentControllerExceptionTest extends BaseControllerTest {

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private AssignmentServiceQuery assignmentServiceQuery;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private com.wanli.academy.backend.repository.UserRepository userRepository;

    private AssignmentCreateRequest createRequest;
    private UUID testAssignmentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testAssignmentId = UUID.randomUUID();
        
        // 创建测试请求
        createRequest = new AssignmentCreateRequest();
        createRequest.setTitle("测试作业");
        createRequest.setDescription("这是一个测试作业的描述");
        createRequest.setDueDate(LocalDateTime.now().plusDays(7));
        createRequest.setTotalScore(100);
        createRequest.setStatus("DRAFT");
        
        // 配置PermissionService的mock行为
        when(permissionService.isTeacher()).thenReturn(true);
        when(permissionService.isAdmin()).thenReturn(false);
        when(permissionService.canAccessAssignment(any())).thenReturn(true);
        when(permissionService.canModifyAssignment(any())).thenReturn(true);
        when(permissionService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(permissionService.getCurrentUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    @DisplayName("获取作业列表时服务异常应抛出异常")
    void should_throwException_when_getAssignmentsServiceFails() throws Exception {
        // Given
        when(assignmentServiceQuery.getAssignmentsByCreator())
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/assignments"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentServiceQuery).getAssignmentsByCreator();
    }

    @Test
    @DisplayName("更新作业时服务异常应抛出异常")
    void should_throwException_when_updateAssignmentServiceFails() throws Exception {
        // Given
        when(assignmentService.updateAssignment(eq(testAssignmentId), any()))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        mockMvc.perform(put("/api/assignments/{id}", testAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentService).updateAssignment(eq(testAssignmentId), any());
    }

    @Test
    @DisplayName("删除作业时服务异常应抛出异常")
    void should_throwException_when_deleteAssignmentServiceFails() throws Exception {
        // Given
        doThrow(new RuntimeException("Delete failed"))
                .when(assignmentService).deleteAssignment(testAssignmentId);

        // When & Then
        mockMvc.perform(delete("/api/assignments/{id}", testAssignmentId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentService).deleteAssignment(testAssignmentId);
    }

    @Test
    @DisplayName("获取作业提交列表时服务异常应抛出异常")
    void should_throwException_when_getAssignmentSubmissionsServiceFails() throws Exception {
        // Given
        when(assignmentServiceQuery.getSubmissionsByAssignment(testAssignmentId))
                .thenThrow(new RuntimeException("Failed to get submissions"));

        // When & Then
        mockMvc.perform(get("/api/assignments/{id}/submissions", testAssignmentId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentServiceQuery).getSubmissionsByAssignment(testAssignmentId);
    }

    @Test
    @DisplayName("获取作业文件列表时服务异常应抛出异常")
    void should_throwException_when_getAssignmentFilesServiceFails() throws Exception {
        // Given
        when(assignmentServiceQuery.getFilesByAssignment(testAssignmentId))
                .thenThrow(new RuntimeException("Failed to get files"));

        // When & Then
        mockMvc.perform(get("/api/assignments/{id}/files", testAssignmentId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentServiceQuery).getFilesByAssignment(testAssignmentId);
    }

    @Test
    @DisplayName("获取即将到期作业时服务异常应抛出异常")
    void should_throwException_when_getAssignmentsDueSoonServiceFails() throws Exception {
        // Given
        when(assignmentServiceQuery.getAssignmentsDueSoon(24))
                .thenThrow(new RuntimeException("Failed to get due soon assignments"));

        // When & Then
        mockMvc.perform(get("/api/assignments/due-soon")
                        .param("hours", "24"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentServiceQuery).getAssignmentsDueSoon(24);
    }

    @Test
    @DisplayName("根据状态获取作业时服务异常应抛出异常")
    void should_throwException_when_getAssignmentsByStatusServiceFails() throws Exception {
        // Given
        when(assignmentServiceQuery.getAssignmentsByStatus("DRAFT"))
                .thenThrow(new RuntimeException("Failed to get assignments by status"));

        // When & Then
        mockMvc.perform(get("/api/assignments/status/{status}", "DRAFT"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentServiceQuery).getAssignmentsByStatus("DRAFT");
    }
}