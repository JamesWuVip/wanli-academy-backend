package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.SubmissionResponse;
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

/**
 * SubmissionService CRUD操作单元测试
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceCrudTest {

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

    @Test
    void should_submitAssignment_when_validDataProvided() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.existsByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID))
            .thenReturn(false);
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.submitAssignment(
            TEST_ASSIGNMENT_ID, TEST_CONTENT, TEST_FILE_PATH);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CONTENT, result.getContent());
        assertEquals("SUBMITTED", result.getStatus());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void should_throwException_when_assignmentNotFound() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, TEST_CONTENT, TEST_FILE_PATH));
        assertEquals("作业不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_pastDeadline() {
        // Given
        testAssignment.setDueDate(LocalDateTime.now().minusDays(1));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, TEST_CONTENT, TEST_FILE_PATH));
        assertEquals("作业已过期", exception.getMessage());
    }

    @Test
    void should_throwException_when_duplicateSubmission() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.existsByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID))
            .thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, TEST_CONTENT, TEST_FILE_PATH));
        assertEquals("已提交过此作业", exception.getMessage());
    }

    @Test
    void should_updateSubmission_when_validDataProvided() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.updateSubmission(
            TEST_SUBMISSION_ID, "Updated content", "/uploads/updated.pdf");

        // Then
        assertNotNull(result);
        verify(submissionRepository).save(testSubmission);
    }

    @Test
    void should_throwException_when_notOwner() {
        // Given
        testSubmission.setStudentId(OTHER_USER_ID);
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> submissionService.updateSubmission(TEST_SUBMISSION_ID, "Updated content", "/uploads/updated.pdf"));
        assertEquals("您无权访问此提交", exception.getMessage());
    }

    @Test
    void should_throwException_when_alreadyGraded() {
        // Given
        testSubmission.setStatus("GRADED");
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.updateSubmission(TEST_SUBMISSION_ID, "Updated content", "/uploads/updated.pdf"));
        assertEquals("已批改的提交不能修改", exception.getMessage());
    }

    @Test
    void should_throwException_when_nullContent() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.existsByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID))
            .thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, null, TEST_FILE_PATH));
        assertEquals("提交内容不能为空", exception.getMessage());
    }

    @Test
    void should_throwException_when_emptyContent() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.existsByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID))
            .thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.submitAssignment(TEST_ASSIGNMENT_ID, "", TEST_FILE_PATH));
        assertEquals("提交内容不能为空", exception.getMessage());
    }

    @Test
    void should_throwException_when_nullAssignmentId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.submitAssignment(null, TEST_CONTENT, TEST_FILE_PATH));
        assertEquals("作业ID不能为空", exception.getMessage());
    }

    @Test
    void should_throwException_when_updateSubmissionNotFound() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.updateSubmission(TEST_SUBMISSION_ID, "Updated content", "/uploads/updated.pdf"));
        assertEquals("提交不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_updatePastDeadline() {
        // Given
        testAssignment.setDueDate(LocalDateTime.now().minusDays(1));
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.updateSubmission(TEST_SUBMISSION_ID, "Updated content", "/uploads/updated.pdf"));
        assertEquals("作业已过期，无法修改提交", exception.getMessage());
    }

    @Test
    void should_throwException_when_updateWithNullContent() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.updateSubmission(TEST_SUBMISSION_ID, null, "/uploads/updated.pdf"));
        assertEquals("提交内容不能为空", exception.getMessage());
    }
}