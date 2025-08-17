package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.dto.SubmissionResponse;
import com.wanli.academy.backend.dto.AssignmentFileResponse;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.entity.AssignmentFile;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import com.wanli.academy.backend.repository.AssignmentFileRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssignmentService 查询操作单元测试
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssignmentServiceQueryTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentFileRepository assignmentFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AssignmentServiceQuery assignmentServiceQuery;

    // 测试常量
    private static final UUID TEST_ASSIGNMENT_ID = UUID.randomUUID();
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_ASSIGNMENT_TITLE = "Test Assignment";
    private static final String TEST_ASSIGNMENT_DESCRIPTION = "Test Description";
    private static final Integer TEST_MAX_SCORE = 100;
    private static final String TEST_STATUS_DRAFT = "DRAFT";
    private static final String TEST_STATUS_PUBLISHED = "PUBLISHED";

    private User testUser;
    private Assignment testAssignment;

    @BeforeEach
    void setUp() {
        // 设置测试用户
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);

        // 设置测试作业
        testAssignment = new Assignment();
        testAssignment.setId(TEST_ASSIGNMENT_ID);
        testAssignment.setTitle(TEST_ASSIGNMENT_TITLE);
        testAssignment.setDescription(TEST_ASSIGNMENT_DESCRIPTION);
        testAssignment.setCreator(testUser);
        testAssignment.setCreatorId(testUser.getId());
        testAssignment.setMaxScore(TEST_MAX_SCORE);
        testAssignment.setStatus(TEST_STATUS_DRAFT);
        testAssignment.setCreatedAt(LocalDateTime.now());
        testAssignment.setUpdatedAt(LocalDateTime.now());
        testAssignment.setDueDate(LocalDateTime.now().plusDays(7));

        // 模拟Spring Security上下文
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(TEST_USERNAME);
        lenient().when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
    }

    @Test
    void should_getAssignmentsByCreator_when_assignmentsExist() {
        // Given
        List<Assignment> assignments = Arrays.asList(testAssignment);
        when(assignmentRepository.findByCreatorIdOrderByCreatedAtDesc(TEST_USER_ID))
            .thenReturn(assignments);

        // When
        List<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsByCreator();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ASSIGNMENT_TITLE, result.get(0).getTitle());
        verify(assignmentRepository).findByCreatorIdOrderByCreatedAtDesc(TEST_USER_ID);
    }

    @Test
    void should_getAssignmentsByCreatorWithPagination_when_assignmentsExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Assignment> assignmentPage = new PageImpl<>(Arrays.asList(testAssignment));
        when(assignmentRepository.findByCreatorIdOrderByCreatedAtDesc(TEST_USER_ID, pageable))
            .thenReturn(assignmentPage);

        // When
        Page<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsByCreatorWithPagination(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(TEST_ASSIGNMENT_TITLE, result.getContent().get(0).getTitle());
        verify(assignmentRepository).findByCreatorIdOrderByCreatedAtDesc(TEST_USER_ID, pageable);
    }

    @Test
    void should_getAssignmentsByStatus_when_assignmentsExist() {
        // Given
        List<Assignment> assignments = Arrays.asList(testAssignment);
        when(assignmentRepository.findByStatusOrderByCreatedAtDesc(TEST_STATUS_DRAFT))
            .thenReturn(assignments);

        // When
        List<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsByStatus(TEST_STATUS_DRAFT);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ASSIGNMENT_TITLE, result.get(0).getTitle());
        verify(assignmentRepository).findByStatusOrderByCreatedAtDesc(TEST_STATUS_DRAFT);
    }

    @Test
    void should_getAssignmentsByStatusWithPagination_when_assignmentsExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Assignment> assignmentPage = new PageImpl<>(Arrays.asList(testAssignment));
        when(assignmentRepository.findByStatusOrderByCreatedAtDesc(TEST_STATUS_DRAFT, pageable))
            .thenReturn(assignmentPage);

        // When
        Page<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsByStatusWithPagination(TEST_STATUS_DRAFT, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(TEST_ASSIGNMENT_TITLE, result.getContent().get(0).getTitle());
        verify(assignmentRepository).findByStatusOrderByCreatedAtDesc(TEST_STATUS_DRAFT, pageable);
    }

    @Test
    void should_getAssignmentsWithFilters_when_allFiltersProvided() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Assignment> assignmentPage = new PageImpl<>(Arrays.asList(testAssignment));
        when(assignmentRepository.findByCreatorIdAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            TEST_USER_ID, TEST_STATUS_DRAFT, "Test", pageable))
            .thenReturn(assignmentPage);

        // When
        Page<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsWithFilters(
            TEST_USER_ID, TEST_STATUS_DRAFT, "Test", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(assignmentRepository).findByCreatorIdAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            TEST_USER_ID, TEST_STATUS_DRAFT, "Test", pageable);
    }

    @Test
    void should_getAssignmentsWithFilters_when_noFiltersProvided() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Assignment> assignmentPage = new PageImpl<>(Arrays.asList(testAssignment));
        when(assignmentRepository.findAllByOrderByCreatedAtDesc(pageable))
            .thenReturn(assignmentPage);

        // When
        Page<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsWithFilters(
            null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(assignmentRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void should_getSubmissionsByAssignment_when_submissionsExist() {
        // Given
        Submission submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setAssignment(testAssignment);
        submission.setStudent(testUser);
        submission.setContent("Test submission");
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());
        
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(TEST_ASSIGNMENT_ID))
            .thenReturn(Arrays.asList(submission));

        // When
        List<SubmissionResponse> result = assignmentServiceQuery.getSubmissionsByAssignment(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(submissionRepository).findByAssignmentIdOrderBySubmittedAtDesc(TEST_ASSIGNMENT_ID);
    }

    @Test
    void should_getFilesByAssignment_when_filesExist() {
        // Given
        AssignmentFile file = new AssignmentFile();
        file.setId(UUID.randomUUID());
        file.setAssignmentId(TEST_ASSIGNMENT_ID);
        file.setFileName("test.pdf");
        file.setFilePath("/files/test.pdf");
        file.setFileSize(1024L);
        file.setFileType("application/pdf");
        file.setUploadedBy(TEST_USER_ID);
        file.setCreatedAt(LocalDateTime.now());
        
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(assignmentFileRepository.findByAssignmentIdOrderByCreatedAtDesc(TEST_ASSIGNMENT_ID))
            .thenReturn(Arrays.asList(file));

        // When
        List<AssignmentFileResponse> result = assignmentServiceQuery.getFilesByAssignment(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test.pdf", result.get(0).getFileName());
        verify(assignmentFileRepository).findByAssignmentIdOrderByCreatedAtDesc(TEST_ASSIGNMENT_ID);
    }

    @Test
    void should_getAssignmentsDueSoon_when_assignmentsDueSoon() {
        // Given
        List<Assignment> assignments = Arrays.asList(testAssignment);
        when(assignmentRepository.findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(assignments);

        // When
        List<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsDueSoon(24);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assignmentRepository).findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void should_returnEmptyList_when_noAssignmentsByCreator() {
        // Given
        when(assignmentRepository.findByCreatorIdOrderByCreatedAtDesc(TEST_USER_ID))
            .thenReturn(Collections.emptyList());

        // When
        List<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsByCreator();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(assignmentRepository).findByCreatorIdOrderByCreatedAtDesc(TEST_USER_ID);
    }

    @Test
    void should_returnEmptyList_when_invalidStatusProvided() {
        // Given
        String invalidStatus = "INVALID_STATUS";
        when(assignmentRepository.findByStatusOrderByCreatedAtDesc(invalidStatus))
            .thenReturn(Collections.emptyList());

        // When
        List<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsByStatus(invalidStatus);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(assignmentRepository).findByStatusOrderByCreatedAtDesc(invalidStatus);
    }

    @Test
    void should_returnEmptyList_when_negativeHoursProvided() {
        // Given
        when(assignmentRepository.findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        // When
        List<AssignmentResponse> result = assignmentServiceQuery.getAssignmentsDueSoon(-1);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(assignmentRepository).findByDueDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}