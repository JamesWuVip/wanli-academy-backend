package com.wanli.academy.backend.service;

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
import static org.mockito.Mockito.*;

/**
 * SubmissionService统计功能测试类
 * 测试提交统计相关功能
 */
@ExtendWith(MockitoExtension.class)
class SubmissionServiceStatisticsTest {

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
    private static final String TEST_USERNAME = "testuser";

    private User testUser;
    private Assignment testAssignment;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        // 设置用户
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // 设置作业
        testAssignment = new Assignment();
        testAssignment.setId(TEST_ASSIGNMENT_ID);
        testAssignment.setTitle("Test Assignment");
        testAssignment.setDescription("Test Description");
        testAssignment.setCreatorId(TEST_USER_ID);
        testAssignment.setDueDate(LocalDateTime.now().plusDays(7));
        testAssignment.setMaxScore(100);
        testAssignment.setStatus("PUBLISHED");
        testAssignment.setCreatedAt(LocalDateTime.now());
        testAssignment.setUpdatedAt(LocalDateTime.now());

        // 设置提交
        testSubmission = new Submission();
        testSubmission.setId(UUID.randomUUID());
        testSubmission.setAssignmentId(TEST_ASSIGNMENT_ID);
        testSubmission.setStudentId(TEST_USER_ID);
        testSubmission.setContent("Test submission content");
        testSubmission.setStatus("SUBMITTED");
        testSubmission.setSubmittedAt(LocalDateTime.now());
        testSubmission.setCreatedAt(LocalDateTime.now());
        testSubmission.setUpdatedAt(LocalDateTime.now());

        // 设置Security Context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
    }

    @Test
    void should_getAssignmentStatistics_when_validAssignment() {
        // Given
        testAssignment.setCreatorId(TEST_USER_ID);
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.countByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(10L);
        when(submissionRepository.countByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "GRADED")).thenReturn(8L);
        when(submissionRepository.countByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "SUBMITTED")).thenReturn(2L);
        
        List<Submission> gradedSubmissions = Arrays.asList(
            createSubmissionWithScore(85),
            createSubmissionWithScore(90)
        );
        when(submissionRepository.findByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "GRADED"))
            .thenReturn(gradedSubmissions);

        // When
        Map<String, Object> result = submissionService.getAssignmentStatistics(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSIGNMENT_ID, result.get("assignmentId"));
        assertEquals("Test Assignment", result.get("assignmentTitle"));
        assertEquals(10L, result.get("totalSubmissions"));
        assertEquals(8L, result.get("gradedSubmissions"));
        assertEquals(2L, result.get("pendingSubmissions"));
        assertEquals(87.5, result.get("averageScore"));
        assertEquals(100, result.get("maxScore"));
    }

    @Test
    void should_throwException_when_statisticsAccessDenied() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> submissionService.getAssignmentStatistics(TEST_ASSIGNMENT_ID));
        assertEquals("您只能查看自己创建的作业统计", exception.getMessage());
    }

    @Test
    void should_getSubmissionStatistics_when_validData() {
        // Given
        testAssignment.setCreatorId(TEST_USER_ID);
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.countByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(10L);
        when(submissionRepository.countByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "GRADED")).thenReturn(8L);
        when(submissionRepository.findAverageScoreByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(85.5);
        when(submissionRepository.findMaxScoreByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(95);
        when(submissionRepository.findMinScoreByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(70);

        // When
        SubmissionService.SubmissionStatistics result = submissionService.getSubmissionStatistics(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getTotalSubmissions());
        assertEquals(8L, result.getGradedSubmissions());
        assertEquals(2L, result.getPendingSubmissions());
        assertEquals(85.5, result.getAverageScore());
        assertEquals(95, result.getMaxScore());
        assertEquals(70, result.getMinScore());
    }

    @Test
    void should_throwException_when_assignmentStatisticsNotFound() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> submissionService.getAssignmentStatistics(TEST_ASSIGNMENT_ID));
        assertEquals("作业不存在", exception.getMessage());
    }

    @Test
    void should_getAssignmentStatistics_when_noSubmissions() {
        // Given
        testAssignment.setCreatorId(TEST_USER_ID);
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.countByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(0L);
        when(submissionRepository.countByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "GRADED")).thenReturn(0L);
        when(submissionRepository.countByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "SUBMITTED")).thenReturn(0L);
        when(submissionRepository.findByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "GRADED"))
            .thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = submissionService.getAssignmentStatistics(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("totalSubmissions"));
        assertEquals(0L, result.get("gradedSubmissions"));
        assertEquals(0L, result.get("pendingSubmissions"));
        assertEquals(0.0, result.get("averageScore"));
        assertEquals(0, result.get("maxScore"));
    }

    @Test
    void should_getSubmissionStatistics_when_noGradedSubmissions() {
        // Given
        testAssignment.setCreatorId(TEST_USER_ID);
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.countByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(5L);
        when(submissionRepository.countByAssignmentIdAndStatus(TEST_ASSIGNMENT_ID, "GRADED")).thenReturn(0L);
        when(submissionRepository.findAverageScoreByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(null);
        when(submissionRepository.findMaxScoreByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(null);
        when(submissionRepository.findMinScoreByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(null);

        // When
        SubmissionService.SubmissionStatistics result = submissionService.getSubmissionStatistics(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getTotalSubmissions());
        assertEquals(0L, result.getGradedSubmissions());
        assertEquals(5L, result.getPendingSubmissions());
        assertNull(result.getAverageScore());
        assertNull(result.getMaxScore());
        assertNull(result.getMinScore());
    }

    private Submission createSubmissionWithScore(int score) {
        Submission submission = new Submission();
        submission.setScore(score);
        return submission;
    }
}