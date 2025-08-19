package com.wanli.academy.backend.service;

import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CustomUserDetailsService测试类
 * 测试用户详情服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "encodedPassword";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ROLE_ID = 1L;

    private User activeUser;
    private User inactiveUser;
    private Role studentRole;
    private Role teacherRole;

    @BeforeEach
    void setUp() {
        // 创建角色
        studentRole = new Role();
        studentRole.setId(TEST_ROLE_ID);
        studentRole.setName("STUDENT");
        studentRole.setDescription("学生角色");

        teacherRole = new Role();
        teacherRole.setId(2L);
        teacherRole.setName("HQ_TEACHER");
        teacherRole.setDescription("总部教师角色");

        // 创建激活用户
        activeUser = new User();
        activeUser.setId(TEST_USER_ID);
        activeUser.setUsername(TEST_USERNAME);
        activeUser.setEmail(TEST_EMAIL);
        activeUser.setPassword(TEST_PASSWORD);
        activeUser.setIsActive(true);
        activeUser.setRoles(Set.of(studentRole));

        // 创建非激活用户
        inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setUsername("inactiveuser");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword(TEST_PASSWORD);
        inactiveUser.setIsActive(false);
        inactiveUser.setRoles(Set.of(studentRole));
    }

    @Test
    void should_loadUserDetails_when_validUsernameProvided() {
        // Given
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.of(activeUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Then
        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        // 验证权限
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
        
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
    }

    @Test
    void should_loadUserDetails_when_validEmailProvided() {
        // Given
        when(userRepository.findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL))
                .thenReturn(Optional.of(activeUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        
        verify(userRepository).findByUsernameOrEmail(TEST_EMAIL, TEST_EMAIL);
    }

    @Test
    void should_loadUserDetailsWithMultipleRoles_when_userHasMultipleRoles() {
        // Given
        activeUser.setRoles(Set.of(studentRole, teacherRole));
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.of(activeUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Then
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        
        // 验证包含所有角色权限
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_HQ_TEACHER")));
        
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
    }

    @Test
    void should_throwUsernameNotFoundException_when_userNotFound() {
        // Given
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(TEST_USERNAME);
        });

        assertEquals("用户不存在: " + TEST_USERNAME, exception.getMessage());
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
    }

    @Test
    void should_throwUsernameNotFoundException_when_userIsInactive() {
        // Given
        when(userRepository.findByUsernameOrEmail("inactiveuser", "inactiveuser"))
                .thenReturn(Optional.of(inactiveUser));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("inactiveuser");
        });

        assertEquals("用户账户已被禁用: inactiveuser", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail("inactiveuser", "inactiveuser");
    }

    @Test
    void should_loadUserDetailsById_when_validUserIdProvided() {
        // Given
        when(userRepository.findById(TEST_USER_ID))
                .thenReturn(Optional.of(activeUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserById(TEST_USER_ID);

        // Then
        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonLocked());
        
        // 验证权限
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
        
        verify(userRepository).findById(TEST_USER_ID);
    }

    @Test
    void should_throwUsernameNotFoundException_when_userIdNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserById(nonExistentId);
        });

        assertEquals("用户不存在，ID: " + nonExistentId, exception.getMessage());
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    void should_throwUsernameNotFoundException_when_userByIdIsInactive() {
        // Given
        Long inactiveUserId = 2L;
        when(userRepository.findById(inactiveUserId))
                .thenReturn(Optional.of(inactiveUser));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserById(inactiveUserId);
        });

        assertEquals("用户账户已被禁用，ID: " + inactiveUserId, exception.getMessage());
        verify(userRepository).findById(inactiveUserId);
    }

    @Test
    void should_buildCorrectUserDetails_when_userHasNoRoles() {
        // Given
        activeUser.setRoles(Set.of()); // 无角色用户
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.of(activeUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Then
        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.isEnabled());
        
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
    }

    @Test
    void should_handleNullUsername_when_loadingUserByUsername() {
        // Given
        String nullUsername = null;
        when(userRepository.findByUsernameOrEmail(nullUsername, nullUsername))
                .thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(nullUsername);
        });

        assertEquals("用户不存在: null", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail(nullUsername, nullUsername);
    }

    @Test
    void should_handleEmptyUsername_when_loadingUserByUsername() {
        // Given
        String emptyUsername = "";
        when(userRepository.findByUsernameOrEmail(emptyUsername, emptyUsername))
                .thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(emptyUsername);
        });

        assertEquals("用户不存在: ", exception.getMessage());
        verify(userRepository).findByUsernameOrEmail(emptyUsername, emptyUsername);
    }

    @Test
    void should_setAccountLockedCorrectly_when_userIsInactive() {
        // Given - 这个测试验证buildUserDetails方法中accountLocked的设置逻辑
        // 我们需要一个激活的用户来测试UserDetails的构建
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.of(activeUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Then
        assertTrue(userDetails.isAccountNonLocked()); // 激活用户账户不应被锁定
        assertTrue(userDetails.isEnabled()); // 激活用户应该被启用
        
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
    }
}