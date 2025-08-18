package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.SubmissionResultDTO;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SubmissionService submissionService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final UUID TEST_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID TEST_SUBMISSION_ID = UUID.randomUUID();
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_CONTENT = "Test submission content";
    private static final String TEST_FILE_PATH = "/uploads/test.pdf";

    private User testUser;
    private User otherUser;
    private Assignment testAssignment;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        // 设置用户
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);

        otherUser = new User();
        otherUser.setId(OTHER_USER_ID);
        otherUser.setUsername("otheruser");

        // 设置作业
        testAssignment = new Assignment();
        testAssignment.setId(TEST_ASSIGNMENT_ID);
        testAssignment.setTitle("Test Assignment");
        testAssignment.setCreatorId(OTHER_USER_ID);
        testAssignment.setMaxScore(100);
        testAssignment.setDueDate(LocalDateTime.now().plusDays(7));

        // 设置提交
        testSubmission = new Submission();
        testSubmission.setId(TEST_SUBMISSION_ID);
        testSubmission.setAssignmentId(TEST_ASSIGNMENT_ID);
        testSubmission.setStudentId(TEST_USER_ID);
        testSubmission.setContent(TEST_CONTENT);
        testSubmission.setFilePath(TEST_FILE_PATH);
        testSubmission.setStatus("SUBMITTED");
        testSubmission.setSubmittedAt(LocalDateTime.now());
        testSubmission.setCreatedAt(LocalDateTime.now());
        testSubmission.setUpdatedAt(LocalDateTime.now());

        // 设置安全上下文
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn(TEST_USERNAME);
        lenient().when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
    }

    // CRUD相关测试已迁移到 SubmissionServiceCrudTest.java

    // 评分相关测试已迁移到 SubmissionServiceGradingTest.java

    // 查询相关测试已迁移到 SubmissionServiceQueryTest.java

    // 统计相关测试已迁移到 SubmissionServiceStatisticsTest.java

    // 结果查询相关测试（Sprint 4新增）
    @Test
    void should_returnSubmissionResult_when_validSubmissionId() {
        // Given
        UUID submissionId = TEST_SUBMISSION_ID;
        
        // 模拟提交记录存在
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(testSubmission));
        
        // 模拟作业存在
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        
        // 模拟学生用户存在
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        
        // When
        SubmissionResultDTO result = submissionService.getSubmissionResult(submissionId);
        
        // Then
        assertNotNull(result);
        assertEquals(submissionId, result.getSubmissionId());
        assertEquals(TEST_ASSIGNMENT_ID, result.getAssignmentId());
        assertNotNull(result.getQuestions());
    }

    @Test
    void should_throwException_when_submissionNotFound() {
        // Given
        UUID nonExistentSubmissionId = UUID.randomUUID();
        when(submissionRepository.findById(nonExistentSubmissionId)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> submissionService.getSubmissionResult(nonExistentSubmissionId));
        assertEquals("提交记录不存在", exception.getMessage());
    }

    // 基础异常处理测试
    @Test
    void should_throwException_when_userNotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, TEST_CONTENT, TEST_FILE_PATH));
        assertEquals("用户未登录", exception.getMessage());
    }

    @Test
    void should_throwException_when_userNotFound() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, TEST_CONTENT, TEST_FILE_PATH));
        assertEquals("用户不存在", exception.getMessage());
    }

    private Submission createSubmissionWithScore(int score) {
        Submission submission = new Submission();
        submission.setScore(score);
        return submission;
    }
}