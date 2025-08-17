package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AuthResponse;
import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.RoleRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService测试类
 * 测试用户认证相关的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;
    private Role userRole;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String ACCESS_TOKEN = "access.token.jwt";
    private static final String REFRESH_TOKEN = "refresh.token.jwt";
    private static final Long JWT_EXPIRATION = 3600000L; // 1 hour
    private static final Long REFRESH_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername(TEST_USERNAME);
        validRegisterRequest.setEmail(TEST_EMAIL);
        validRegisterRequest.setPassword(TEST_PASSWORD);
        validRegisterRequest.setFirstName("Test");
        validRegisterRequest.setLastName("User");
        validRegisterRequest.setPhoneNumber("1234567890");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsernameOrEmail(TEST_USERNAME);
        validLoginRequest.setPassword(TEST_PASSWORD);

        userRole = new Role("USER", "普通用户");
        userRole.setId(1L);
        userRole.setIsActive(true);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhoneNumber("1234567890");
        testUser.setIsActive(true);
        testUser.addRole(userRole);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== register方法测试 ====================

    @Test
    void should_registerUser_when_validRequest() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateTokenFromUsername(TEST_USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshTokenFromUsername(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getJwtExpiration()).thenReturn(JWT_EXPIRATION);
        when(jwtService.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        // When
        AuthResponse result = authService.register(validRegisterRequest);

        // Then
        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.getAccessToken());
        assertEquals(REFRESH_TOKEN, result.getRefreshToken());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_EMAIL, result.getEmail());
        assertTrue(result.getRoles().contains("USER"));

        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(roleRepository).findByName("USER");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateTokenFromUsername(TEST_USERNAME);
        verify(jwtService).generateRefreshTokenFromUsername(TEST_USERNAME);
    }

    @Test
    void should_throwRuntimeException_when_usernameExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(validRegisterRequest);
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void should_throwRuntimeException_when_emailExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(validRegisterRequest);
        });

        assertEquals("邮箱已存在", exception.getMessage());
        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void should_createUserRole_when_defaultRoleNotExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(userRole);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateTokenFromUsername(TEST_USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshTokenFromUsername(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getJwtExpiration()).thenReturn(JWT_EXPIRATION);
        when(jwtService.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        // When
        AuthResponse result = authService.register(validRegisterRequest);

        // Then
        assertNotNull(result);
        verify(roleRepository).findByName("USER");
        verify(roleRepository).save(any(Role.class));
        verify(userRepository).save(any(User.class));
    }

    // ==================== login方法测试 ====================

    @Test
    void should_loginUser_when_validCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateTokenFromUsername(TEST_USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshTokenFromUsername(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getJwtExpiration()).thenReturn(JWT_EXPIRATION);
        when(jwtService.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        // When
        AuthResponse result = authService.login(validLoginRequest);

        // Then
        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.getAccessToken());
        assertEquals(REFRESH_TOKEN, result.getRefreshToken());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(TEST_USERNAME, result.getUsername());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_throwRuntimeException_when_invalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(validLoginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
    }

    @Test
    void should_throwRuntimeException_when_userNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(validLoginRequest);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
    }

    @Test
    void should_throwRuntimeException_when_userIsInactive() {
        // Given
        testUser.setIsActive(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME))
                .thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(validLoginRequest);
        });

        assertEquals("用户账户已被禁用", exception.getMessage());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsernameOrEmail(TEST_USERNAME, TEST_USERNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== refreshToken方法测试 ====================

    @Test
    void should_refreshToken_when_validToken() {
        // Given
        when(jwtService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(jwtService.generateTokenFromUsername(TEST_USERNAME)).thenReturn(ACCESS_TOKEN);
        when(jwtService.generateRefreshTokenFromUsername(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(jwtService.getJwtExpiration()).thenReturn(JWT_EXPIRATION);
        when(jwtService.getRefreshExpiration()).thenReturn(REFRESH_EXPIRATION);

        // When
        AuthResponse result = authService.refreshToken(REFRESH_TOKEN);

        // Then
        assertNotNull(result);
        assertEquals(ACCESS_TOKEN, result.getAccessToken());
        assertEquals(REFRESH_TOKEN, result.getRefreshToken());
        assertEquals(testUser.getId(), result.getUserId());

        verify(jwtService).isTokenValid(REFRESH_TOKEN);
        verify(jwtService).extractUsername(REFRESH_TOKEN);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void should_throwRuntimeException_when_invalidRefreshToken() {
        // Given
        when(jwtService.isTokenValid(REFRESH_TOKEN)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(REFRESH_TOKEN);
        });

        assertEquals("刷新令牌失败: 刷新令牌无效或已过期", exception.getMessage());
        verify(jwtService).isTokenValid(REFRESH_TOKEN);
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void should_throwRuntimeException_when_userNotFoundForRefresh() {
        // Given
        when(jwtService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(REFRESH_TOKEN);
        });

        assertEquals("刷新令牌失败: 用户不存在", exception.getMessage());
        verify(jwtService).isTokenValid(REFRESH_TOKEN);
        verify(jwtService).extractUsername(REFRESH_TOKEN);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void should_throwRuntimeException_when_userInactiveForRefresh() {
        // Given
        testUser.setIsActive(false);
        when(jwtService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(REFRESH_TOKEN);
        });

        assertEquals("刷新令牌失败: 用户账户已被禁用", exception.getMessage());
        verify(jwtService).isTokenValid(REFRESH_TOKEN);
        verify(jwtService).extractUsername(REFRESH_TOKEN);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    // ==================== 工具方法测试 ====================

    @Test
    void should_returnTrue_when_usernameAvailable() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);

        // When
        boolean result = authService.isUsernameAvailable(TEST_USERNAME);

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername(TEST_USERNAME);
    }

    @Test
    void should_returnFalse_when_usernameNotAvailable() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When
        boolean result = authService.isUsernameAvailable(TEST_USERNAME);

        // Then
        assertFalse(result);
        verify(userRepository).existsByUsername(TEST_USERNAME);
    }

    @Test
    void should_returnTrue_when_emailAvailable() {
        // Given
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        // When
        boolean result = authService.isEmailAvailable(TEST_EMAIL);

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail(TEST_EMAIL);
    }

    @Test
    void should_returnFalse_when_emailNotAvailable() {
        // Given
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When
        boolean result = authService.isEmailAvailable(TEST_EMAIL);

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail(TEST_EMAIL);
    }

    @Test
    void should_returnUser_when_getUserByUsername() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = authService.getUserByUsername(TEST_USERNAME);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(TEST_USERNAME, result.get().getUsername());
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void should_returnEmpty_when_getUserByUsernameNotFound() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When
        Optional<User> result = authService.getUserByUsername(TEST_USERNAME);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void should_returnUser_when_getUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = authService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void should_returnEmpty_when_getUserByIdNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = authService.getUserById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }
}