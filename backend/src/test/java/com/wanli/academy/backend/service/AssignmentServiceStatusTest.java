package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AssignmentResponse;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.User;
import org.springframework.security.access.AccessDeniedException;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AssignmentService状态管理测试类
 * 测试作业状态更新、权限控制等功能
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceStatusTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private static final UUID TEST_ASSIGNMENT_ID = UUID.randomUUID();
    private static final Long TEST_USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private Assignment testAssignment;
    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername("testuser");
        // User实体使用roles集合，不是单个role字段

        otherUser = new User();
        otherUser.setId(OTHER_USER_ID);
        otherUser.setUsername("otheruser");

        // 创建测试作业
        testAssignment = new Assignment();
        testAssignment.setId(TEST_ASSIGNMENT_ID);
        testAssignment.setTitle("Test Assignment");
        testAssignment.setDescription("Test Description");
        testAssignment.setCreatorId(TEST_USER_ID);
        testAssignment.setStatus("DRAFT");
        testAssignment.setCreatedAt(LocalDateTime.now());
        testAssignment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void should_updateAssignmentStatus_when_validStatusProvided() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        verify(assignmentRepository).save(testAssignment);
    }

    @Test
    void should_throwException_when_assignmentNotFound() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void should_throwException_when_userNotFound() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void should_throwException_when_accessDeniedForStatusUpdate() {
        // Given
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(OTHER_USER_ID)).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void should_throwException_when_invalidStatusTransition() {
        // Given
        testAssignment.setStatus("CLOSED");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "DRAFT");
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void should_allowStatusTransition_from_draftToPublished() {
        // Given
        testAssignment.setStatus("DRAFT");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");

        // Then
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        verify(assignmentRepository).save(testAssignment);
    }

    @Test
    void should_allowStatusTransition_from_publishedToClosed() {
        // Given
        testAssignment.setStatus("PUBLISHED");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "CLOSED");

        // Then
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(assignmentRepository).save(testAssignment);
    }

    @Test
    void should_allowStatusTransition_from_draftToClosed() {
        // Given
        testAssignment.setStatus("DRAFT");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "CLOSED");

        // Then
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(assignmentRepository).save(testAssignment);
    }

    @Test
    void should_preventStatusTransition_from_closedToAnyOther() {
        // Given
        testAssignment.setStatus("CLOSED");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When & Then - 测试从CLOSED到DRAFT的转换
        assertThrows(RuntimeException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "DRAFT");
        });

        // When & Then - 测试从CLOSED到PUBLISHED的转换
        assertThrows(RuntimeException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void should_preventStatusTransition_from_publishedToDraft() {
        // Given
        testAssignment.setStatus("PUBLISHED");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "DRAFT");
        });

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void should_allowSameStatusUpdate() {
        // Given
        testAssignment.setStatus("PUBLISHED");
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(testAssignment);

        // When
        AssignmentResponse result = assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");

        // Then
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        verify(assignmentRepository).save(testAssignment);
    }

    @Test
    void should_updateTimestamp_when_statusUpdated() {
        // Given
        LocalDateTime originalUpdatedAt = testAssignment.getUpdatedAt();
        when(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).thenReturn(Optional.of(testAssignment));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment savedAssignment = invocation.getArgument(0);
            savedAssignment.setUpdatedAt(LocalDateTime.now());
            return savedAssignment;
        });

        // When
        AssignmentResponse result = assignmentService.updateAssignmentStatus(TEST_ASSIGNMENT_ID, "PUBLISHED");

        // Then
        assertThat(result.getUpdatedAt()).isAfter(originalUpdatedAt);
        verify(assignmentRepository).save(testAssignment);
    }
}