package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AssignmentCreateRequest;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AssignmentService单元测试
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AssignmentServiceTest {

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
    private AssignmentService assignmentService;

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
    private static final String TEST_STATUS_CLOSED = "CLOSED";

    private User testUser;
    private Assignment testAssignment;
    private AssignmentCreateRequest testRequest;

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

        // 设置测试请求
        testRequest = new AssignmentCreateRequest();
        testRequest.setTitle(TEST_ASSIGNMENT_TITLE);
        testRequest.setDescription(TEST_ASSIGNMENT_DESCRIPTION);
        testRequest.setTotalScore(TEST_MAX_SCORE);
        testRequest.setStatus(TEST_STATUS_DRAFT);
        testRequest.setDueDate(LocalDateTime.now().plusDays(7));

        // 模拟Spring Security上下文
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(TEST_USERNAME);
        lenient().when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
    }

    @Test
    void should_createAssignment_when_validDataProvided() {
        // Given
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.createAssignment(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSIGNMENT_TITLE, result.getTitle());
        assertEquals(TEST_ASSIGNMENT_DESCRIPTION, result.getDescription());
        assertEquals(TEST_USER_ID, result.getCreatorId());
        assertEquals(TEST_USERNAME, result.getCreatorUsername());
        assertEquals(TEST_MAX_SCORE, result.getTotalScore());
        assertEquals(TEST_STATUS_DRAFT, result.getStatus());

        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void should_throwException_when_userNotFound() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.createAssignment(testRequest));
        assertEquals("当前用户不存在", exception.getMessage());
    }

    // 查询相关测试已移至 AssignmentServiceQueryTest.java











    // 状态相关测试已移至 AssignmentServiceStatusTest.java





    // getAssignmentById 方法已迁移到 AssignmentServiceQuery，相关测试在 AssignmentServiceQueryTest 中

    @Test
    void should_updateAssignment_when_validDataProvided() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.updateAssignment(TEST_ASSIGNMENT_ID, testRequest);

        // Then
        assertNotNull(result);
        verify(assignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void should_deleteAssignment_when_noSubmissionsExist() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.countByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(0L);

        // When
        assertDoesNotThrow(() -> assignmentService.deleteAssignment(TEST_ASSIGNMENT_ID));

        // Then
        verify(assignmentRepository).findById(TEST_ASSIGNMENT_ID);
        verify(submissionRepository).countByAssignmentId(TEST_ASSIGNMENT_ID);
        verify(assignmentFileRepository).deleteByAssignmentId(TEST_ASSIGNMENT_ID);
        verify(assignmentRepository).delete(testAssignment);
    }

    @Test
    void should_throwException_when_assignmentHasSubmissions() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(submissionRepository.countByAssignmentId(TEST_ASSIGNMENT_ID)).thenReturn(1L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.deleteAssignment(TEST_ASSIGNMENT_ID));
        assertEquals("无法删除已有提交记录的作业", exception.getMessage());
    }

    // 提交相关测试已移至 AssignmentServiceSubmissionTest.java

    // 文件相关测试已移至 AssignmentServiceFileTest.java



    @Test
    void should_validateAssignmentOwnership_when_userIsOwner() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When
        Assignment result = assignmentService.validateAssignmentOwnership(TEST_ASSIGNMENT_ID);

        // Then
        assertNotNull(result);
        assertEquals(TEST_ASSIGNMENT_ID, result.getId());
        verify(assignmentRepository).findById(TEST_ASSIGNMENT_ID);
    }

    @Test
    void should_throwException_when_assignmentNotFoundForValidation() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.validateAssignmentOwnership(TEST_ASSIGNMENT_ID));
        assertEquals("作业不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_accessDeniedForValidation() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        testAssignment.setCreator(otherUser);
        
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> assignmentService.validateAssignmentOwnership(TEST_ASSIGNMENT_ID));
        assertEquals("您无权访问此作业", exception.getMessage());
    }

    // 边界条件和异常处理测试
    @Test
    void should_throwException_when_nullRequestProvided() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> assignmentService.createAssignment(null));
    }

    @Test
    void should_createAssignment_when_emptyTitleProvided() {
        // Given
        testRequest.setTitle("");
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.createAssignment(testRequest);

        // Then
        assertNotNull(result);
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void should_createAssignment_when_negativeMaxScoreProvided() {
        // Given
        testRequest.setTotalScore(-10);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.createAssignment(testRequest);

        // Then
        assertNotNull(result);
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void should_createAssignment_when_pastDueDateProvided() {
        // Given
        testRequest.setDueDate(LocalDateTime.now().minusDays(1));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);
 
        // When
        AssignmentResponse result = assignmentService.createAssignment(testRequest);

        // Then
        assertNotNull(result);
        // The service should accept past due dates and create the assignment
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void should_throwException_when_updateAssignmentNotFound() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.updateAssignment(TEST_ASSIGNMENT_ID, testRequest));
        assertEquals("作业不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_accessDeniedForUpdate() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testAssignment.setCreator(otherUser);
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> assignmentService.updateAssignment(TEST_ASSIGNMENT_ID, testRequest));
        assertEquals("您无权访问此作业", exception.getMessage());
    }

    @Test
    void should_throwException_when_deleteAssignmentNotFound() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.deleteAssignment(TEST_ASSIGNMENT_ID));
        assertEquals("作业不存在", exception.getMessage());
    }

    @Test
    void should_throwException_when_accessDeniedForDelete() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testAssignment.setCreator(otherUser);
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> assignmentService.deleteAssignment(TEST_ASSIGNMENT_ID));
        assertEquals("您无权访问此作业", exception.getMessage());
    }










}