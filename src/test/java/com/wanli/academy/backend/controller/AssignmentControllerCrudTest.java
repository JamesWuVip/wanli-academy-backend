package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;

import com.wanli.academy.backend.dto.AssignmentCreateRequest;
import com.wanli.academy.backend.dto.AssignmentResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import org.mockito.MockitoAnnotations;

/**
 * AssignmentController CRUD操作测试类
 * 测试作业的创建、读取、更新、删除操作
 */
@WebMvcTest(controllers = AssignmentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("作业控制器CRUD操作测试")
class AssignmentControllerCrudTest extends BaseControllerTest {

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private AssignmentServiceQuery assignmentServiceQuery;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private com.wanli.academy.backend.repository.UserRepository userRepository;

    private com.wanli.academy.backend.entity.User mockUser;

    private AssignmentCreateRequest createRequest;
    private AssignmentResponse assignmentResponse;
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
        
        // 创建测试响应
        assignmentResponse = new AssignmentResponse();
        assignmentResponse.setId(testAssignmentId);
        assignmentResponse.setTitle("测试作业");
        assignmentResponse.setDescription("这是一个测试作业的描述");
        assignmentResponse.setCreatorId(TEST_USER_ID);
        assignmentResponse.setCreatorUsername(TEST_USERNAME);
        assignmentResponse.setDueDate(LocalDateTime.now().plusDays(7));
        assignmentResponse.setTotalScore(100);
        assignmentResponse.setStatus("DRAFT");
        assignmentResponse.setCreatedAt(LocalDateTime.now());
        assignmentResponse.setUpdatedAt(LocalDateTime.now());
        
        // 配置PermissionService的mock行为
        when(permissionService.isTeacher()).thenReturn(true);
        when(permissionService.isAdmin()).thenReturn(false);
        when(permissionService.canAccessAssignment(any())).thenReturn(true);
        when(permissionService.canModifyAssignment(any())).thenReturn(true);
        when(permissionService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(permissionService.getCurrentUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    @DisplayName("创建作业成功")
    void should_createAssignment_when_validDataProvided() throws Exception {
        // Given
        when(assignmentService.createAssignment(any()))
                .thenReturn(assignmentResponse);

        // When & Then
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$.title").value("测试作业"))
                .andExpect(jsonPath("$.description").value("这是一个测试作业的描述"))
                .andExpect(jsonPath("$.creatorId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.creatorUsername").value(TEST_USERNAME))
                .andExpect(jsonPath("$.totalScore").value(100))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(assignmentService).createAssignment(any());
    }

    @Test
    @DisplayName("创建作业时标题为空应返回400")
    void should_returnBadRequest_when_creatingAssignmentWithEmptyTitle() throws Exception {
        // Given
        createRequest.setTitle("");

        // When & Then
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentService, never()).createAssignment(any());
    }

    @Test
    @DisplayName("无权限创建作业应返回400")
    void should_returnBadRequest_when_creatingAssignmentWithoutPermission() throws Exception {
        // Given
        when(permissionService.isTeacher()).thenReturn(false);
        when(permissionService.isAdmin()).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentService, never()).createAssignment(any());
    }

    @Test
    @DisplayName("根据ID获取作业详情成功")
    void should_getAssignmentById_when_assignmentExists() throws Exception {
        // Given
        when(assignmentServiceQuery.getAssignmentById(testAssignmentId))
                .thenReturn(assignmentResponse);

        // When & Then
        mockMvc.perform(get("/api/assignments/{id}", testAssignmentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$.title").value("测试作业"));

        verify(assignmentServiceQuery).getAssignmentById(testAssignmentId);
    }

    @Test
    @DisplayName("获取不存在的作业应返回400")
    void should_returnBadRequest_when_assignmentNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(assignmentServiceQuery.getAssignmentById(nonExistentId))
                .thenThrow(new RuntimeException("Assignment not found"));

        // When & Then
        mockMvc.perform(get("/api/assignments/{id}", nonExistentId))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(assignmentServiceQuery).getAssignmentById(nonExistentId);
    }

    @Test
    @DisplayName("更新作业成功")
    void should_updateAssignment_when_validDataProvided() throws Exception {
        // Given
        AssignmentResponse updatedResponse = new AssignmentResponse();
        updatedResponse.setId(testAssignmentId);
        updatedResponse.setTitle("更新后的作业");
        updatedResponse.setDescription("更新后的描述");
        updatedResponse.setCreatorId(TEST_USER_ID);
        updatedResponse.setCreatorUsername(TEST_USERNAME);
        updatedResponse.setDueDate(LocalDateTime.now().plusDays(10));
        updatedResponse.setTotalScore(120);
        updatedResponse.setStatus("PUBLISHED");
        updatedResponse.setCreatedAt(LocalDateTime.now());
        updatedResponse.setUpdatedAt(LocalDateTime.now());

        when(assignmentService.updateAssignment(eq(testAssignmentId), any()))
                .thenReturn(updatedResponse);

        AssignmentCreateRequest updateRequest = new AssignmentCreateRequest();
        updateRequest.setTitle("更新后的作业");
        updateRequest.setDescription("更新后的描述");
        updateRequest.setDueDate(LocalDateTime.now().plusDays(10));
        updateRequest.setTotalScore(120);
        updateRequest.setStatus("PUBLISHED");

        // When & Then
        mockMvc.perform(put("/api/assignments/{id}", testAssignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$.title").value("更新后的作业"))
                .andExpect(jsonPath("$.description").value("更新后的描述"))
                .andExpect(jsonPath("$.totalScore").value(120))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(assignmentService).updateAssignment(eq(testAssignmentId), any());
    }

    @Test
    @DisplayName("删除作业成功")
    void should_deleteAssignment_when_assignmentExists() throws Exception {
        // Given
        doNothing().when(assignmentService).deleteAssignment(testAssignmentId);

        // When & Then
        mockMvc.perform(delete("/api/assignments/{id}", testAssignmentId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(assignmentService).deleteAssignment(testAssignmentId);
    }
}