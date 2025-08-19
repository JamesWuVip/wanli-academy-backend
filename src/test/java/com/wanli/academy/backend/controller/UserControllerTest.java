package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.base.BaseControllerTest;
import com.wanli.academy.backend.config.SecurityConfig;
import com.wanli.academy.backend.config.JwtAuthenticationEntryPoint;
import com.wanli.academy.backend.config.JwtAuthenticationFilter;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController测试类
 * 测试用户相关的HTTP请求处理
 */
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
@DisplayName("UserController测试")
class UserControllerTest extends BaseControllerTest {

    private User testUser;
    private Role testRole;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_STUDENT");
        testRole.setDescription("学生角色");

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("测试");
        testUser.setLastName("用户");
        testUser.setPhoneNumber("13800138000");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);

        // 创建测试用户详情
        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(TEST_USERNAME)
                .password("encoded_password")
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))
                .build();

        // 配置JWT服务模拟行为 - 使用更具体的token匹配
        String tokenWithoutBearer = VALID_JWT_TOKEN.replace("Bearer ", "");
        when(jwtService.extractUsername(tokenWithoutBearer)).thenReturn(TEST_USERNAME);
        when(jwtService.isTokenValid(tokenWithoutBearer, testUserDetails)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(testUserDetails);
        
        // 配置AuthService模拟行为
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("应该成功获取当前用户信息_当用户已认证且存在")
    void should_getCurrentUser_when_authenticatedUserExists() throws Exception {
        // Given
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                .andExpect(jsonPath("$.data.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("测试"))
                .andExpect(jsonPath("$.data.lastName").value("用户"))
                .andExpect(jsonPath("$.data.phoneNumber").value("13800138000"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该返回未认证错误_当用户未认证")
    void should_returnUnauthorized_when_userNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(getWithoutAuth("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("应该返回用户不存在错误_当用户不在数据库中")
    void should_returnNotFound_when_userNotExistsInDatabase() throws Exception {
        // Given
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户不存在"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该返回禁用错误_当用户账户被禁用")
    void should_returnForbidden_when_userAccountDisabled() throws Exception {
        // Given
        testUser.setIsActive(false);
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户账户已被禁用"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该返回服务器错误_当获取用户信息时发生异常")
    void should_returnInternalServerError_when_exceptionOccursGettingUser() throws Exception {
        // Given
        when(authService.getUserByUsername(TEST_USERNAME)).thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("获取用户信息失败"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该成功获取用户个人资料_当用户已认证")
    void should_getUserProfile_when_authenticatedUser() throws Exception {
        // Given
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        getWithAuth("/api/users/profile", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取用户信息成功"))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该返回未认证状态_当用户未认证时检查认证状态")
    void should_returnUnauthenticatedStatus_when_checkingAuthStatusWithoutAuth() throws Exception {
        // When & Then
        mockMvc.perform(getWithoutAuth("/api/users/auth-status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("应该返回已认证状态_当用户已认证时检查认证状态")
    void should_returnAuthenticatedStatus_when_checkingAuthStatusWithAuth() throws Exception {
        // When & Then
        getWithAuth("/api/users/auth-status", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.message").value("用户已认证"))
                .andExpect(jsonPath("$.authorities").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该返回已认证状态_当用户已认证时")
    void should_returnAuthenticatedStatus_when_userIsAuthenticated() throws Exception {
        // When & Then
        getWithAuth("/api/users/auth-status", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("用户已认证"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("应该正确处理多角色用户_当用户拥有多个角色")
    void should_handleMultipleRoles_when_userHasMultipleRoles() throws Exception {
        // Given
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");
        adminRole.setDescription("管理员角色");
        
        Set<Role> roles = new HashSet<>();
        roles.add(testRole);
        roles.add(adminRole);
        testUser.setRoles(roles);
        
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles.length()").value(2));
    }

    @Test
    @DisplayName("应该正确处理空角色用户_当用户没有角色")
    void should_handleUserWithoutRoles_when_userHasNoRoles() throws Exception {
        // Given
        testUser.setRoles(new HashSet<>());
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles.length()").value(0));
    }

    @Test
    @DisplayName("应该正确处理空字段_当用户某些字段为空")
    void should_handleNullFields_when_userHasNullFields() throws Exception {
        // Given
        testUser.setFirstName(null);
        testUser.setLastName(null);
        testUser.setPhoneNumber(null);
        when(authService.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        getWithAuth("/api/users/me", VALID_JWT_TOKEN.replace("Bearer ", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").isEmpty())
                .andExpect(jsonPath("$.data.lastName").isEmpty())
                .andExpect(jsonPath("$.data.phoneNumber").isEmpty());
    }
}