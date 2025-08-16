package com.wanli.academy.backend.service;

import com.wanli.academy.backend.config.TestConfig;
import com.wanli.academy.backend.dto.AuthResponse;
import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.RoleRepository;
import com.wanli.academy.backend.repository.UserRepository;
import com.wanli.academy.backend.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService单元测试
 * 测试用户注册、登录、令牌刷新等核心认证功能
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("认证服务测试")
class AuthServiceTest {
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private RoleRepository roleRepository;
    
    @MockBean
    private JwtService jwtService;
    
    @MockBean
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthService authService;
    
    private User testUser;
    private Role userRole;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    
    @BeforeEach
    void setUp() {
        // 创建测试数据
        testUser = TestDataUtil.createTestUser(passwordEncoder);
        userRole = TestDataUtil.createUserRole();
        registerRequest = TestDataUtil.createRegisterRequest();
        loginRequest = TestDataUtil.createLoginRequest();
        
        // 重置Mock对象
        reset(userRepository, roleRepository, jwtService, authenticationManager);
    }
    
    @Nested
    @DisplayName("用户注册测试")
    class UserRegistrationTests {
        
        @Test
        @DisplayName("应该成功注册新用户")
        void shouldRegisterNewUserSuccessfully() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateTokenFromUsername(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshTokenFromUsername(anyString())).thenReturn("refresh-token");
            
            // When
            AuthResponse response = authService.register(registerRequest);
            
            // Then
            assertNotNull(response, "注册响应不应为空");
            assertEquals("access-token", response.getAccessToken(), "访问令牌应匹配");
            assertEquals("refresh-token", response.getRefreshToken(), "刷新令牌应匹配");
            assertEquals("Bearer", response.getTokenType(), "令牌类型应为Bearer");
            assertEquals(testUser.getId(), response.getUserId(), "用户ID应匹配");
            assertEquals(testUser.getUsername(), response.getUsername(), "用户名应匹配");
            assertEquals(testUser.getEmail(), response.getEmail(), "邮箱应匹配");
            
            // 验证方法调用
            verify(userRepository).existsByUsername(registerRequest.getUsername());
            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(roleRepository).findByName("USER");
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateTokenFromUsername(anyString());
            verify(jwtService).generateRefreshTokenFromUsername(anyString());
        }
        
        @Test
        @DisplayName("应该拒绝重复用户名注册")
        void shouldRejectDuplicateUsername() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.register(registerRequest);
            });
            
            assertTrue(exception.getMessage().contains("用户名已存在"), "异常消息应包含用户名已存在");
            verify(userRepository).existsByUsername(registerRequest.getUsername());
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("应该拒绝重复邮箱注册")
        void shouldRejectDuplicateEmail() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.register(registerRequest);
            });
            
            assertTrue(exception.getMessage().contains("邮箱已存在"), "异常消息应包含邮箱已存在");
            verify(userRepository).existsByUsername(registerRequest.getUsername());
            verify(userRepository).existsByEmail(registerRequest.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }
        
        @Test
        @DisplayName("应该处理角色不存在的情况")
        void shouldHandleMissingUserRole() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateTokenFromUsername(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshTokenFromUsername(anyString())).thenReturn("refresh-token");
            
            // When
            AuthResponse response = authService.register(registerRequest);
            
            // Then
            assertNotNull(response, "注册响应不应为空");
            verify(roleRepository).findByName("USER");
            verify(roleRepository).save(any(Role.class)); // 验证创建了新角色
            verify(userRepository).save(any(User.class));
        }
        
        @Test
        @DisplayName("注册时应该正确编码密码")
        void shouldEncodePasswordDuringRegistration() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
            when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                // 验证密码已被编码
                assertNotEquals(registerRequest.getPassword(), savedUser.getPassword(), "密码应该被编码");
                assertTrue(passwordEncoder.matches(registerRequest.getPassword(), savedUser.getPassword()), "编码后的密码应该匹配原密码");
                return testUser;
            });
            when(jwtService.generateTokenFromUsername(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshTokenFromUsername(anyString())).thenReturn("refresh-token");
            
            // When
            authService.register(registerRequest);
            
            // Then
            verify(userRepository).save(any(User.class));
        }
    }
    
    @Nested
    @DisplayName("用户登录测试")
    class UserLoginTests {
        
        @Test
        @DisplayName("应该成功使用用户名登录")
        void shouldLoginWithUsernameSuccessfully() {
            // Given
            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
            when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
            when(jwtService.generateTokenFromUsername(anyString())).thenReturn("access-token");
            when(jwtService.generateRefreshTokenFromUsername(anyString())).thenReturn("refresh-token");
            
            // When
            AuthResponse response = authService.login(loginRequest);
            
            // Then
            assertNotNull(response, "登录响应不应为空");
            assertEquals("access-token", response.getAccessToken(), "访问令牌应匹配");
            assertEquals("refresh-token", response.getRefreshToken(), "刷新令牌应匹配");
            assertEquals(testUser.getUsername(), response.getUsername(), "用户名应匹配");
            
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail());
            verify(jwtService).generateTokenFromUsername(anyString());
            verify(jwtService).generateRefreshTokenFromUsername(anyString());
        }
        
        @Test
        @DisplayName("应该成功使用邮箱登录")
        void shouldLoginWithEmailSuccessfully() {
            // Given
            LoginRequest emailLoginRequest = TestDataUtil.createEmailLoginRequest();
            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
            when(userRepository.findByUsernameOrEmail(emailLoginRequest.getUsernameOrEmail(), emailLoginRequest.getUsernameOrEmail())).thenReturn(Optional.of(testUser));
            when(jwtService.generateTokenFromUsername(any())).thenReturn("access-token");
            when(jwtService.generateRefreshTokenFromUsername(any())).thenReturn("refresh-token");
            
            // When
            AuthResponse response = authService.login(emailLoginRequest);
            
            // Then
            assertNotNull(response, "登录响应不应为空");
            assertEquals(testUser.getEmail(), response.getEmail(), "邮箱应匹配");
            
            verify(userRepository).findByUsernameOrEmail(emailLoginRequest.getUsernameOrEmail(), emailLoginRequest.getUsernameOrEmail());
        }
        
        @Test
        @DisplayName("应该拒绝错误凭证登录")
        void shouldRejectInvalidCredentials() {
            // Given
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("用户名或密码错误"));
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.login(loginRequest);
            });
            
            assertTrue(exception.getMessage().contains("用户名或密码错误"), "异常消息应包含凭证错误信息");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, never()).findByUsername(any());
        }
        
        @Test
        @DisplayName("应该拒绝未激活用户登录")
        void shouldRejectInactiveUserLogin() {
            // Given
            User inactiveUser = TestDataUtil.createInactiveUser(passwordEncoder);
            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
            when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())).thenReturn(Optional.of(inactiveUser));
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.login(loginRequest);
            });
            
            assertTrue(exception.getMessage().contains("账户已被禁用"), "异常消息应包含账户禁用信息");
            verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail());
            verify(jwtService, never()).generateTokenFromUsername(anyString());
        }
        
        @Test
        @DisplayName("应该处理用户不存在的情况")
        void shouldHandleUserNotFound() {
            // Given
            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
            when(userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())).thenReturn(Optional.empty());
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.login(loginRequest);
            });
            
            assertTrue(exception.getMessage().contains("用户不存在"), "异常消息应包含用户不存在信息");
            verify(userRepository).findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail());
        }
    }
    
    @Nested
    @DisplayName("令牌刷新测试")
    class TokenRefreshTests {
        
        @Test
        @DisplayName("应该成功刷新有效令牌")
        void shouldRefreshValidToken() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(jwtService.extractUsername(refreshToken)).thenReturn(testUser.getUsername());
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
            when(jwtService.generateTokenFromUsername(anyString())).thenReturn("new-access-token");
            when(jwtService.generateRefreshTokenFromUsername(anyString())).thenReturn("new-refresh-token");
            
            // When
            AuthResponse response = authService.refreshToken(refreshToken);
            
            // Then
            assertNotNull(response, "刷新响应不应为空");
            assertEquals("new-access-token", response.getAccessToken(), "新访问令牌应匹配");
            assertEquals("new-refresh-token", response.getRefreshToken(), "新刷新令牌应匹配");
            assertEquals(testUser.getUsername(), response.getUsername(), "用户名应匹配");
            
            verify(jwtService).extractUsername(refreshToken);
            verify(userRepository).findByUsername(testUser.getUsername());
            verify(jwtService).isTokenValid(refreshToken);
            verify(jwtService).generateTokenFromUsername(anyString());
            verify(jwtService).generateRefreshTokenFromUsername(anyString());
        }
        
        @Test
        @DisplayName("应该拒绝无效刷新令牌")
        void shouldRejectInvalidRefreshToken() {
            // Given
            String invalidRefreshToken = "invalid-refresh-token";
            when(jwtService.extractUsername(invalidRefreshToken)).thenReturn(testUser.getUsername());
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(jwtService.isTokenValid(invalidRefreshToken)).thenReturn(false);
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.refreshToken(invalidRefreshToken);
            });
            
            assertTrue(exception.getMessage().contains("刷新令牌无效"), "异常消息应包含令牌无效信息");
            verify(jwtService).isTokenValid(invalidRefreshToken);
            verify(jwtService, never()).generateTokenFromUsername(anyString());
        }
        
        @Test
        @DisplayName("应该处理刷新令牌对应用户不存在的情况")
        void shouldHandleRefreshTokenUserNotFound() {
            // Given
            String refreshToken = "valid-refresh-token";
            when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
            when(jwtService.extractUsername(refreshToken)).thenReturn("nonexistentuser");
            when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.refreshToken(refreshToken);
            });
            
            assertTrue(exception.getMessage().contains("刷新令牌失败") && exception.getMessage().contains("用户不存在"), "异常消息应包含刷新令牌失败和用户不存在信息");
            verify(jwtService).isTokenValid(refreshToken);
            verify(jwtService).extractUsername(refreshToken);
            verify(userRepository).findByUsername("nonexistentuser");
        }
    }
    
    @Nested
    @DisplayName("用户可用性检查测试")
    class UserAvailabilityTests {
        
        @Test
        @DisplayName("应该正确检查用户名可用性")
        void shouldCheckUsernameAvailability() {
            // Given
            String availableUsername = "availableuser";
            String unavailableUsername = "unavailableuser";
            when(userRepository.existsByUsername(availableUsername)).thenReturn(false);
            when(userRepository.existsByUsername(unavailableUsername)).thenReturn(true);
            
            // When & Then
            assertTrue(authService.isUsernameAvailable(availableUsername), "可用用户名应返回true");
            assertFalse(authService.isUsernameAvailable(unavailableUsername), "不可用用户名应返回false");
            
            verify(userRepository).existsByUsername(availableUsername);
            verify(userRepository).existsByUsername(unavailableUsername);
        }
        
        @Test
        @DisplayName("应该正确检查邮箱可用性")
        void shouldCheckEmailAvailability() {
            // Given
            String availableEmail = "available@example.com";
            String unavailableEmail = "unavailable@example.com";
            when(userRepository.existsByEmail(availableEmail)).thenReturn(false);
            when(userRepository.existsByEmail(unavailableEmail)).thenReturn(true);
            
            // When & Then
            assertTrue(authService.isEmailAvailable(availableEmail), "可用邮箱应返回true");
            assertFalse(authService.isEmailAvailable(unavailableEmail), "不可用邮箱应返回false");
            
            verify(userRepository).existsByEmail(availableEmail);
            verify(userRepository).existsByEmail(unavailableEmail);
        }
    }
    
    @Nested
    @DisplayName("用户查询测试")
    class UserQueryTests {
        
        @Test
        @DisplayName("应该成功根据用户名查询用户")
        void shouldGetUserByUsername() {
            // Given
            when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            
            // When
            Optional<User> result = authService.getUserByUsername(testUser.getUsername());
            
            // Then
            assertTrue(result.isPresent(), "应该找到用户");
            assertEquals(testUser.getUsername(), result.get().getUsername(), "用户名应匹配");
            verify(userRepository).findByUsername(testUser.getUsername());
        }
        
        @Test
        @DisplayName("应该成功根据用户ID查询用户")
        void shouldGetUserById() {
            // Given
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            
            // When
            Optional<User> result = authService.getUserById(testUser.getId());
            
            // Then
            assertTrue(result.isPresent(), "应该找到用户");
            assertEquals(testUser.getId(), result.get().getId(), "用户ID应匹配");
            verify(userRepository).findById(testUser.getId());
        }
        
        @Test
        @DisplayName("查询不存在的用户应返回空Optional")
        void shouldReturnEmptyForNonExistentUser() {
            // Given
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
            when(userRepository.findById(999L)).thenReturn(Optional.empty());
            
            // When
            Optional<User> userByUsername = authService.getUserByUsername("nonexistent");
            Optional<User> userById = authService.getUserById(999L);
            
            // Then
            assertFalse(userByUsername.isPresent(), "不存在的用户名应返回空Optional");
            assertFalse(userById.isPresent(), "不存在的用户ID应返回空Optional");
        }
    }
    
    @Nested
    @DisplayName("边界条件和异常处理测试")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("应该处理空用户名或邮箱登录")
        void shouldHandleNullUsernameOrEmailLogin() {
            // Given
            LoginRequest nullLoginRequest = new LoginRequest();
            nullLoginRequest.setUsernameOrEmail(null);
            nullLoginRequest.setPassword("password");
            
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                authService.login(nullLoginRequest);
            }, "空用户名或邮箱应抛出异常");
        }
        
        @Test
        @DisplayName("应该处理空刷新令牌")
        void shouldHandleNullRefreshToken() {
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                authService.refreshToken(null);
            }, "空刷新令牌应抛出异常");
        }
        
        @Test
        @DisplayName("应该处理数据库异常")
        void shouldHandleDatabaseException() {
            // Given
            when(userRepository.existsByUsername(registerRequest.getUsername()))
                    .thenThrow(new RuntimeException("数据库连接失败"));
            
            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                authService.register(registerRequest);
            });
            
            assertTrue(exception.getMessage().contains("数据库连接失败"), "应该传播数据库异常");
        }
    }
}