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
 * SubmissionService 查询功能单元测试
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceQueryTest {

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
    private static final Long TEACHER_USER_ID = 3L;
    private static final UUID TEST_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID TEST_SUBMISSION_ID = UUID.randomUUID();
    private static final String TEST_USERNAME = "testuser";
    private static final String OTHER_USERNAME = "otheruser";
    private static final String TEACHER_USERNAME = "teacher";
    private static final String TEST_CONTENT = "Test submission content";
    private static final String TEST_FILE_PATH = "/uploads/test.pdf";

    private User testUser;
    private User otherUser;
    private User teacherUser;
    private Assignment testAssignment;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        // 设置学生用户
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);

        // 设置其他学生用户
        otherUser = new User();
        otherUser.setId(OTHER_USER_ID);
        otherUser.setUsername(OTHER_USERNAME);

        // 设置教师用户
        teacherUser = new User();
        teacherUser.setId(TEACHER_USER_ID);
        teacherUser.setUsername(TEACHER_USERNAME);

        // 设置作业
        testAssignment = new Assignment();
        testAssignment.setId(TEST_ASSIGNMENT_ID);
        testAssignment.setTitle("Test Assignment");
        testAssignment.setCreatorId(TEACHER_USER_ID);
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
    void should_getStudentSubmission_when_submissionExists() {
        // Given
        when(submissionRepository.findByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID))
            .thenReturn(Optional.of(testSubmission));

        // When
        Optional<SubmissionResponse> result = submissionService.getStudentSubmission(TEST_ASSIGNMENT_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TEST_CONTENT, result.get().getContent());
        assertEquals("SUBMITTED", result.get().getStatus());
        verify(submissionRepository).findByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID);
    }

    @Test
    void should_returnNull_when_submissionNotFound() {
        // Given
        when(submissionRepository.findByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID))
            .thenReturn(Optional.empty());

        // When
        Optional<SubmissionResponse> result = submissionService.getStudentSubmission(TEST_ASSIGNMENT_ID);

        // Then
        assertFalse(result.isPresent());
        verify(submissionRepository).findByAssignmentIdAndStudentId(TEST_ASSIGNMENT_ID, TEST_USER_ID);
    }

    @Test
    void should_getStudentSubmissions_when_submissionsExist() {
        // Given
        List<Submission> submissions = Arrays.asList(testSubmission);
        when(submissionRepository.findByStudentIdOrderBySubmittedAtDesc(TEST_USER_ID))
            .thenReturn(submissions);

        // When
        List<SubmissionResponse> result = submissionService.getStudentSubmissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_CONTENT, result.get(0).getContent());
        verify(submissionRepository).findByStudentIdOrderBySubmittedAtDesc(TEST_USER_ID);
    }

    @Test
    void should_getPendingGradeSubmissions_when_submissionsExist() {
        // Given
        List<Submission> submissions = Arrays.asList(testSubmission);
        when(submissionRepository.findPendingGradeSubmissions())
            .thenReturn(submissions);

        // When
        List<SubmissionResponse> result = submissionService.getPendingGradeSubmissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SUBMITTED", result.get(0).getStatus());
        verify(submissionRepository).findPendingGradeSubmissions();
    }

    @Test
    void should_getSubmissionById_when_userIsStudent() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));

        // When
        SubmissionResponse result = submissionService.getSubmissionById(TEST_SUBMISSION_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CONTENT, result.getContent());
        verify(submissionRepository).findById(TEST_SUBMISSION_ID);
    }

    @Test
    void should_getSubmissionById_when_userIsTeacher() {
        // Given
        testSubmission.setStudentId(OTHER_USER_ID); // 不是当前用户的提交
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(authentication.getName()).thenReturn(TEACHER_USERNAME);
        when(userRepository.findByUsername(TEACHER_USERNAME)).thenReturn(Optional.of(teacherUser));

        // When
        SubmissionResponse result = submissionService.getSubmissionById(TEST_SUBMISSION_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_CONTENT, result.getContent());
        verify(submissionRepository).findById(TEST_SUBMISSION_ID);
        verify(assignmentRepository).findById(TEST_ASSIGNMENT_ID);
    }

    @Test
    void should_throwException_when_accessDenied() {
        // Given
        testSubmission.setStudentId(OTHER_USER_ID); // 不是当前用户的提交
        testAssignment.setCreatorId(999L); // 不是当前用户创建的作业
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> submissionService.getSubmissionById(TEST_SUBMISSION_ID));
        assertEquals("您无权访问此提交", exception.getMessage());
    }

    @Test
    void should_throwException_when_nullAssignmentIdForStudentSubmission() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.getStudentSubmission(null));
        assertEquals("作业ID不能为空", exception.getMessage());
    }

    @Test
    void should_throwException_when_submissionNotFound() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.getSubmissionById(TEST_SUBMISSION_ID));
        assertEquals("提交记录不存在", exception.getMessage());
    }

    @Test
    void should_returnEmptyList_when_noPendingGradeSubmissions() {
        // Given
        when(submissionRepository.findPendingGradeSubmissions())
            .thenReturn(Collections.emptyList());

        // When
        List<SubmissionResponse> result = submissionService.getPendingGradeSubmissions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(submissionRepository).findPendingGradeSubmissions();
    }

    @Test
    void should_returnEmptyList_when_noStudentSubmissions() {
        // Given
        when(submissionRepository.findByStudentIdOrderBySubmittedAtDesc(TEST_USER_ID))
            .thenReturn(Collections.emptyList());

        // When
        List<SubmissionResponse> result = submissionService.getStudentSubmissions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(submissionRepository).findByStudentIdOrderBySubmittedAtDesc(TEST_USER_ID);
    }

    @Test
    void should_throwException_when_userNotAuthenticated() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.getStudentSubmissions());
        assertEquals("用户未登录", exception.getMessage());
    }

    @Test
    void should_throwException_when_userNotFound() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.getStudentSubmissions());
        assertEquals("当前用户不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_databaseError() {
        // Given
        when(submissionRepository.findByStudentIdOrderBySubmittedAtDesc(TEST_USER_ID))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.getStudentSubmissions());
        assertEquals("Database connection failed", exception.getMessage());
    }
}