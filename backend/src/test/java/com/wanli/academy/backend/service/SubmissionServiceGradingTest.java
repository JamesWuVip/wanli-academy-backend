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
 * SubmissionService 评分功能单元测试
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceGradingTest {

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
    private static final Long TEACHER_USER_ID = 2L;
    private static final UUID TEST_ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID TEST_SUBMISSION_ID = UUID.randomUUID();
    private static final String TEST_USERNAME = "testuser";
    private static final String TEACHER_USERNAME = "teacher";
    private static final String TEST_CONTENT = "Test submission content";
    private static final String TEST_FILE_PATH = "/uploads/test.pdf";

    private User testUser;
    private User teacherUser;
    private Assignment testAssignment;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        // 设置学生用户
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);

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
        lenient().when(authentication.getName()).thenReturn(TEACHER_USERNAME);
        lenient().when(userRepository.findByUsername(TEACHER_USERNAME)).thenReturn(Optional.of(teacherUser));
    }

    @Test
    void should_gradeSubmission_when_validDataProvided() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.gradeSubmission(
            TEST_SUBMISSION_ID, 85, "Good work!");

        // Then
        assertNotNull(result);
        verify(submissionRepository).save(testSubmission);
        assertEquals("GRADED", testSubmission.getStatus());
        assertEquals(85, testSubmission.getScore());
        assertEquals("Good work!", testSubmission.getFeedback());
    }

    @Test
    void should_throwException_when_notAuthorized() {
        // Given
        testAssignment.setCreatorId(999L); // 不同的创建者
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> submissionService.gradeSubmission(TEST_SUBMISSION_ID, 85, "Good work!"));
        assertEquals("您无权批改此作业", exception.getMessage());
    }

    @Test
    void should_throwException_when_invalidScore() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then - 测试负分
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.gradeSubmission(TEST_SUBMISSION_ID, -1, "Invalid score"));
        assertEquals("分数不能为负数", exception1.getMessage());
    }

    @Test
    void should_throwException_when_gradeSubmissionNotFound() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> submissionService.gradeSubmission(TEST_SUBMISSION_ID, 85, "Good work!"));
        assertEquals("提交不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_negativeScore() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.gradeSubmission(TEST_SUBMISSION_ID, -10, "Negative score"));
        assertEquals("分数不能为负数", exception.getMessage());
    }

    @Test
    void should_throwException_when_scoreExceedsMaximum() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.gradeSubmission(TEST_SUBMISSION_ID, 150, "Score too high"));
        assertEquals("分数不能超过满分", exception.getMessage());
    }

    @Test
    void should_allowRegrading_when_alreadyGraded() {
        // Given
        testSubmission.setStatus("GRADED");
        testSubmission.setScore(75);
        testSubmission.setFeedback("Previous feedback");
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.gradeSubmission(
            TEST_SUBMISSION_ID, 90, "Updated feedback");

        // Then
        assertNotNull(result);
        verify(submissionRepository).save(testSubmission);
        assertEquals(90, testSubmission.getScore());
        assertEquals("Updated feedback", testSubmission.getFeedback());
    }

    @Test
    void should_gradeWithZeroScore_when_validZeroScore() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.gradeSubmission(
            TEST_SUBMISSION_ID, 0, "Needs improvement");

        // Then
        assertNotNull(result);
        verify(submissionRepository).save(testSubmission);
        assertEquals(0, testSubmission.getScore());
        assertEquals("Needs improvement", testSubmission.getFeedback());
    }

    @Test
    void should_gradeWithMaxScore_when_validMaxScore() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.gradeSubmission(
            TEST_SUBMISSION_ID, 100, "Perfect work!");

        // Then
        assertNotNull(result);
        verify(submissionRepository).save(testSubmission);
        assertEquals(100, testSubmission.getScore());
        assertEquals("Perfect work!", testSubmission.getFeedback());
    }

    @Test
    void should_gradeWithEmptyFeedback_when_noFeedbackProvided() {
        // Given
        when(submissionRepository.findById(TEST_SUBMISSION_ID)).thenReturn(Optional.of(testSubmission));
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.save(any(Submission.class))).thenReturn(testSubmission);

        // When
        SubmissionResponse result = submissionService.gradeSubmission(
            TEST_SUBMISSION_ID, 85, null);

        // Then
        assertNotNull(result);
        verify(submissionRepository).save(testSubmission);
        assertEquals(85, testSubmission.getScore());
        assertNull(testSubmission.getFeedback());
    }
}