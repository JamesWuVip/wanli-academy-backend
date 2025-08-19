package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;

import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.AssignmentFileResponse;
import com.wanli.academy.backend.service.AssignmentServiceQuery;
import com.wanli.academy.backend.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import org.mockito.MockitoAnnotations;

/**
 * AssignmentController 查询操作测试类
 * 测试作业的查询、列表获取等操作
 */
@WebMvcTest(controllers = AssignmentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("作业控制器查询操作测试")
class AssignmentControllerQueryTest extends BaseControllerTest {

    @MockBean
    private AssignmentServiceQuery assignmentServiceQuery;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private com.wanli.academy.backend.repository.UserRepository userRepository;

    private AssignmentResponse assignmentResponse;
    private UUID testAssignmentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testAssignmentId = UUID.randomUUID();
        
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
        when(permissionService.getCurrentUserId()).thenReturn(TEST_USER_ID);
        when(permissionService.getCurrentUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    @DisplayName("获取作业列表-无认证测试")
    void should_getAssignments_when_noAuthenticationRequired() throws Exception {
        // Given
        when(assignmentServiceQuery.getAssignmentsByCreator())
                .thenReturn(Arrays.asList(assignmentResponse));

        // When & Then
        mockMvc.perform(get("/api/assignments"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取作业列表成功")
    void should_getAssignments_when_requestIsValid() throws Exception {
        // Given
        List<AssignmentResponse> assignments = Arrays.asList(assignmentResponse);
        when(assignmentServiceQuery.getAssignmentsByCreator())
                .thenReturn(assignments);

        // When & Then
        mockMvc.perform(get("/api/assignments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$[0].title").value("测试作业"));

        verify(assignmentServiceQuery).getAssignmentsByCreator();
    }

    @Test
    @DisplayName("获取作业提交列表成功")
    void should_getAssignmentSubmissions_when_assignmentExists() throws Exception {
        // Given
        SubmissionResponse submissionResponse = new SubmissionResponse();
        submissionResponse.setId(UUID.randomUUID());
        submissionResponse.setAssignmentId(testAssignmentId);
        submissionResponse.setStudentId(TEST_USER_ID);
        submissionResponse.setStudentUsername("student1");
        submissionResponse.setSubmittedAt(LocalDateTime.now());
        submissionResponse.setStatus("SUBMITTED");
        
        List<SubmissionResponse> submissions = Arrays.asList(submissionResponse);
        when(assignmentServiceQuery.getSubmissionsByAssignment(testAssignmentId))
                .thenReturn(submissions);

        // When & Then
        mockMvc.perform(get("/api/assignments/{id}/submissions", testAssignmentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assignmentId").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$[0].studentUsername").value("student1"));

        verify(assignmentServiceQuery).getSubmissionsByAssignment(testAssignmentId);
    }

    @Test
    @DisplayName("获取作业文件列表成功")
    void should_getAssignmentFiles_when_assignmentExists() throws Exception {
        // Given
        AssignmentFileResponse fileResponse = new AssignmentFileResponse();
        fileResponse.setId(UUID.randomUUID());
        fileResponse.setAssignmentId(testAssignmentId);
        fileResponse.setFileName("test-file.pdf");
        fileResponse.setFileSize(1024L);
        fileResponse.setFileType("application/pdf");
        fileResponse.setCreatedAt(LocalDateTime.now());
        
        List<AssignmentFileResponse> files = Arrays.asList(fileResponse);
        when(assignmentServiceQuery.getFilesByAssignment(testAssignmentId))
                .thenReturn(files);

        // When & Then
        mockMvc.perform(get("/api/assignments/{id}/files", testAssignmentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assignmentId").value(testAssignmentId.toString()))
                .andExpect(jsonPath("$[0].fileName").value("test-file.pdf"));

        verify(assignmentServiceQuery).getFilesByAssignment(testAssignmentId);
    }

    @Test
    @DisplayName("获取即将到期的作业列表成功")
    void should_getAssignmentsDueSoon_when_requestIsValid() throws Exception {
        // Given
        List<AssignmentResponse> assignments = Arrays.asList(assignmentResponse);
        when(assignmentServiceQuery.getAssignmentsDueSoon(24))
                .thenReturn(assignments);

        // When & Then
        mockMvc.perform(get("/api/assignments/due-soon")
                        .param("hours", "24"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testAssignmentId.toString()));

        verify(assignmentServiceQuery).getAssignmentsDueSoon(24);
    }

    @Test
    @DisplayName("根据状态获取作业列表成功")
    void should_getAssignmentsByStatus_when_statusIsValid() throws Exception {
        // Given
        List<AssignmentResponse> assignments = Arrays.asList(assignmentResponse);
        when(assignmentServiceQuery.getAssignmentsByStatus("DRAFT"))
                .thenReturn(assignments);

        // When & Then
        mockMvc.perform(get("/api/assignments/status/{status}", "DRAFT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));

        verify(assignmentServiceQuery).getAssignmentsByStatus("DRAFT");
    }

    @Test
    @DisplayName("未认证访问应返回200")
    void should_returnOk_when_accessingWithoutAuthentication() throws Exception {
        // Given
        when(assignmentServiceQuery.getAssignmentsByCreator())
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/assignments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(assignmentServiceQuery).getAssignmentsByCreator();
    }
}