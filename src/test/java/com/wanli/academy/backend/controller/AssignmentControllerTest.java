package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;

import com.wanli.academy.backend.dto.AssignmentCreateRequest;
import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.AssignmentFileResponse;
import com.wanli.academy.backend.service.AssignmentService;
import com.wanli.academy.backend.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
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
 * AssignmentController测试类 - 主测试类
 * 
 * 注意：为了提高代码可维护性，测试已按功能拆分为以下文件：
 * - AssignmentControllerCrudTest.java: CRUD操作测试（创建、读取、更新、删除）
 * - AssignmentControllerQueryTest.java: 查询操作测试（列表获取、筛选等）
 * - AssignmentControllerExceptionTest.java: 异常处理测试
 * 
 * 本文件保留基础配置和通用设置
 */
@WebMvcTest(controllers = AssignmentController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@ActiveProfiles("test")
@DisplayName("作业控制器测试 - 主测试类")
class AssignmentControllerTest extends BaseControllerTest {

    @MockBean
    private AssignmentService assignmentService;

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

    // 所有具体的测试方法已迁移到专门的测试类中：
    // - CRUD操作测试 -> AssignmentControllerCrudTest.java
    // - 查询操作测试 -> AssignmentControllerQueryTest.java  
    // - 异常处理测试 -> AssignmentControllerExceptionTest.java
}