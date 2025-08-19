package com.wanli.academy.backend.service;

import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.AssignmentFile;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentFileRepository;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PermissionService单元测试
 * 测试权限控制逻辑的正确性
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("权限服务测试")
class PermissionServiceTest {

    // 测试常量
    private static final UUID TEST_HOMEWORK_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final String TEST_HOMEWORK_TITLE = "测试作业";
    private static final String TEST_HOMEWORK_DESCRIPTION = "测试作业描述";

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentFileRepository assignmentFileRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PermissionService permissionService;

    private User adminUser;
    private User teacherUser;
    private User studentUser;
    private Assignment assignment;
    private Submission submission;
    private AssignmentFile assignmentFile;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);

        // 创建测试用户
        adminUser = createTestUser(1L, "admin", "ADMIN");
        teacherUser = createTestUser(2L, "teacher", "HQ_TEACHER");
        studentUser = createTestUser(3L, "student", "STUDENT");

        // 创建测试作业
        assignment = createTestAssignment();

        // 创建测试提交
        submission = createTestSubmission();

        // 创建测试文件
        assignmentFile = createTestAssignmentFile();
    }

    @Test
    @DisplayName("管理员权限检查")
    void should_checkAdminPermission_when_userRoleIsVerified() {
        // 管理员用户
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.isAdmin());

        // 非管理员用户
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assertFalse(permissionService.isAdmin());
    }

    @Test
    @DisplayName("教师权限检查")
    void should_checkTeacherPermission_when_userRoleIsVerified() {
        // 教师用户
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assertTrue(permissionService.isTeacher());

        // 学生用户
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        assertFalse(permissionService.isTeacher());
    }

    @Test
    @DisplayName("学生权限检查")
    void should_checkStudentPermission_when_userRoleIsVerified() {
        // 学生用户
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        assertTrue(permissionService.isStudent());

        // 教师用户
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assertTrue(permissionService.isStudent()); // 教师也应该有学生权限
    }

    @Test
    @DisplayName("作业访问权限检查")
    void should_checkAssignmentAccess_when_permissionIsVerified() {
        UUID assignmentId = assignment.getId();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

        // 管理员可以访问任何作业
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.canAccessAssignment(assignmentId));

        // 教师可以访问自己创建的作业
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assignment.setCreatorId(2L);
        assertTrue(permissionService.canAccessAssignment(assignmentId));

        // 学生可以访问已发布的作业
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        assignment.setStatus("PUBLISHED");
        assertTrue(permissionService.canAccessAssignment(assignmentId));

        // 学生不能访问未发布的作业
        assignment.setStatus("DRAFT");
        assertFalse(permissionService.canAccessAssignment(assignmentId));
    }

    @Test
    @DisplayName("作业修改权限检查")
    void should_checkAssignmentModification_when_permissionIsVerified() {
        UUID assignmentId = assignment.getId();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

        // 管理员可以修改任何作业
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.canModifyAssignment(assignmentId));

        // 教师可以修改自己创建的作业
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assignment.setCreatorId(2L);
        assertTrue(permissionService.canModifyAssignment(assignmentId));

        // 教师不能修改其他人创建的作业
        assignment.setCreatorId(4L);
        assertFalse(permissionService.canModifyAssignment(assignmentId));

        // 学生不能修改作业
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        assertFalse(permissionService.canModifyAssignment(assignmentId));
    }

    @Test
    @DisplayName("提交访问权限检查")
    void should_checkSubmissionAccess_when_permissionIsVerified() {
        UUID submissionId = submission.getId();
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));

        // 管理员可以访问任何提交
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.canAccessSubmission(submissionId));

        // 学生可以访问自己的提交
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        submission.setStudentId(3L);
        assertTrue(permissionService.canAccessSubmission(submissionId));

        // 学生不能访问其他人的提交
        submission.setStudentId(4L);
        assertFalse(permissionService.canAccessSubmission(submissionId));

        // 教师可以访问相关作业的提交
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        lenient().when(assignmentRepository.findById(submission.getAssignmentId())).thenReturn(Optional.of(assignment));
        assignment.setCreatorId(2L);
        assertTrue(permissionService.canAccessSubmission(submissionId));
    }

    @Test
    @DisplayName("提交批改权限检查")
    void should_checkSubmissionGrading_when_permissionIsVerified() {
        UUID submissionId = submission.getId();
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(assignmentRepository.findById(submission.getAssignmentId())).thenReturn(Optional.of(assignment));

        // 管理员可以批改任何提交
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.canGradeSubmission(submissionId));

        // 教师可以批改相关作业的提交
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assignment.setCreatorId(2L);
        assertTrue(permissionService.canGradeSubmission(submissionId));

        // 教师不能批改无关作业的提交
        assignment.setCreatorId(4L);
        assertFalse(permissionService.canGradeSubmission(submissionId));

        // 学生不能批改提交
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        assertFalse(permissionService.canGradeSubmission(submissionId));
    }

    @Test
    @DisplayName("文件访问权限检查")
    void should_checkFileAccess_when_permissionIsVerified() {
        UUID fileId = assignmentFile.getId();
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.of(assignmentFile));

        // 管理员可以访问任何文件
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.canAccessFile(fileId));

        // 文件上传者可以访问
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assignmentFile.setUploadedBy(2L);
        assertTrue(permissionService.canAccessFile(fileId));

        // 非上传者需要检查作业权限
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        when(assignmentRepository.findById(assignmentFile.getAssignmentId())).thenReturn(Optional.of(assignment));
        assignment.setStatus("PUBLISHED");
        assertTrue(permissionService.canAccessFile(fileId));
    }

    @Test
    @DisplayName("文件删除权限检查")
    void should_checkFileDeletion_when_permissionIsVerified() {
        UUID fileId = assignmentFile.getId();
        when(assignmentFileRepository.findById(fileId)).thenReturn(Optional.of(assignmentFile));

        // 管理员可以删除任何文件
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertTrue(permissionService.canDeleteFile(fileId));

        // 文件上传者可以删除
        when(authentication.getName()).thenReturn("teacher");
        when(userRepository.findByUsername("teacher")).thenReturn(Optional.of(teacherUser));
        assignmentFile.setUploadedBy(2L);
        assertTrue(permissionService.canDeleteFile(fileId));

        // 教师可以删除作业相关文件
        assignmentFile.setUploadedBy(4L); // 其他人上传的文件
        when(assignmentRepository.findById(assignmentFile.getAssignmentId())).thenReturn(Optional.of(assignment));
        assignment.setCreatorId(2L);
        assertTrue(permissionService.canDeleteFile(fileId));

        // 学生不能删除其他人的文件
        when(authentication.getName()).thenReturn("student");
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        assertFalse(permissionService.canDeleteFile(fileId));
    }

    @Test
    @DisplayName("未认证用户权限检查")
    void should_denyAccess_when_userIsUnauthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertFalse(permissionService.isAdmin());
        assertFalse(permissionService.isTeacher());
        assertFalse(permissionService.isStudent());
        assertFalse(permissionService.canAccessAssignment(UUID.randomUUID()));
    }

    @Test
    @DisplayName("不存在的资源权限检查")
    void should_denyAccess_when_resourceNotFound() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            // 设置管理员用户
            when(authentication.getName()).thenReturn(adminUser.getUsername());
            when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));
            
            // Mock SecurityContextHolder
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            UUID nonExistentId = UUID.randomUUID();
            
            // Mock repository返回空结果
            when(assignmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            when(submissionRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            when(assignmentFileRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // 验证作业确实不存在
            Optional<Assignment> result = assignmentRepository.findById(nonExistentId);
            assertFalse(result.isPresent(), "作业应该不存在");
            
            // 验证用户是管理员
            assertTrue(permissionService.isAdmin(), "用户应该是管理员");
            
            // 不存在的资源应该返回false，即使是管理员
            // 这是正确的行为：即使是管理员，也不能访问不存在的资源
            assertFalse(permissionService.canAccessAssignment(nonExistentId), "管理员不应该能访问不存在的作业");
            assertFalse(permissionService.canAccessSubmission(nonExistentId), "管理员不应该能访问不存在的提交");
            assertFalse(permissionService.canAccessFile(nonExistentId), "管理员不应该能访问不存在的文件");
        }
    }

    // ==================== 辅助方法 ====================

    private User createTestUser(Long id, String username, String roleName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setFirstName("Test");
        user.setLastName(username);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Role role = new Role();
        role.setId(Long.valueOf(roleName.hashCode() % 1000));
        role.setName("ROLE_" + roleName);
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return user;
    }

    private Assignment createTestAssignment() {
        Assignment assignment = new Assignment();
        assignment.setId(TEST_HOMEWORK_ID);
        assignment.setTitle(TEST_HOMEWORK_TITLE);
        assignment.setDescription(TEST_HOMEWORK_DESCRIPTION);
        assignment.setCreatorId(2L); // 默认由教师创建
        assignment.setStatus("DRAFT");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        return assignment;
    }

    private Submission createTestSubmission() {
        Submission submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setAssignmentId(TEST_HOMEWORK_ID);
        submission.setStudentId(3L); // 默认由学生提交
        submission.setStatus("SUBMITTED");
        submission.setCreatedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());
        return submission;
    }

    private AssignmentFile createTestAssignmentFile() {
        AssignmentFile file = new AssignmentFile();
        file.setId(UUID.randomUUID());
        file.setAssignmentId(TEST_HOMEWORK_ID);
        file.setFileName("test-file.pdf");
        file.setOriginalFileName("测试文件.pdf");
        file.setFilePath("/uploads/test-file.pdf");
        file.setFileSize(1024L);
        file.setMimeType("application/pdf");
        file.setUploadedBy(2L); // 默认由教师上传
        file.setCreatedAt(LocalDateTime.now());
        file.setUpdatedAt(LocalDateTime.now());
        return file;
    }
}