package com.wanli.academy.backend.util;

import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试数据工具类
 * 用于创建测试所需的各种数据对象
 */
public class TestDataUtil {
    
    // 测试用户数据
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "password123";
    public static final String TEST_FIRST_NAME = "Test";
    public static final String TEST_LAST_NAME = "User";
    public static final String TEST_PHONE = "1234567890";
    
    // 管理员测试数据
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_EMAIL = "admin@example.com";
    public static final String ADMIN_PASSWORD = "admin123";
    
    // JWT测试数据
    public static final String TEST_JWT_SECRET = "testSecretKeyForJwtTokenGenerationInTestEnvironmentOnly123456789";
    public static final long TEST_JWT_EXPIRATION = 3600000L; // 1小时
    public static final long TEST_REFRESH_EXPIRATION = 86400000L; // 24小时
    
    /**
     * 创建测试用户实体
     * @param passwordEncoder 密码编码器
     * @return 测试用户
     */
    public static User buildTestUser(PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setId(1L);
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setFirstName(TEST_FIRST_NAME);
        user.setLastName(TEST_LAST_NAME);
        user.setPhoneNumber(TEST_PHONE);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // 设置用户角色
        Set<Role> roles = new HashSet<>();
        roles.add(buildUserRole());
        user.setRoles(roles);
        
        return user;
    }
    
    /**
     * 创建管理员用户实体
     * @param passwordEncoder 密码编码器
     * @return 管理员用户
     */
    public static User buildAdminUser(PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setId(2L);
        user.setUsername(ADMIN_USERNAME);
        user.setEmail(ADMIN_EMAIL);
        user.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setPhoneNumber("9876543210");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // 设置管理员角色
        Set<Role> roles = new HashSet<>();
        roles.add(buildAdminRole());
        user.setRoles(roles);
        
        return user;
    }
    
    /**
     * 创建未激活的测试用户
     * @param passwordEncoder 密码编码器
     * @return 未激活用户
     */
    public static User buildInactiveUser(PasswordEncoder passwordEncoder) {
        User user = buildTestUser(passwordEncoder);
        user.setId(3L);
        user.setUsername("inactiveuser");
        user.setEmail("inactive@example.com");
        user.setIsActive(false);
        return user;
    }
    
    /**
     * 创建用户角色
     * @return 用户角色
     */
    public static Role buildUserRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName("USER");
        role.setDescription("普通用户角色");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }
    
    /**
     * 创建管理员角色
     * @return 管理员角色
     */
    public static Role buildAdminRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName("ADMIN");
        role.setDescription("管理员角色");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }
    
    /**
     * 创建注册请求DTO
     * @return 注册请求
     */
    public static RegisterRequest buildRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(TEST_USERNAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setFirstName(TEST_FIRST_NAME);
        request.setLastName(TEST_LAST_NAME);
        request.setPhoneNumber(TEST_PHONE);
        return request;
    }
    
    /**
     * 创建无效的注册请求DTO（用户名为空）
     * @return 无效注册请求
     */
    public static RegisterRequest buildInvalidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(""); // 无效用户名
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setFirstName(TEST_FIRST_NAME);
        request.setLastName(TEST_LAST_NAME);
        request.setPhoneNumber(TEST_PHONE);
        return request;
    }
    
    /**
     * 创建登录请求DTO
     * @return 登录请求
     */
    public static LoginRequest buildLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);
        return request;
    }
    
    /**
     * 创建使用邮箱的登录请求DTO
     * @return 登录请求
     */
    public static LoginRequest buildEmailLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        return request;
    }
    
    /**
     * 创建无效的登录请求DTO（密码错误）
     * @return 无效登录请求
     */
    public static LoginRequest buildInvalidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(TEST_USERNAME);
        request.setPassword("wrongpassword");
        return request;
    }
    
    /**
     * 创建管理员登录请求DTO
     * @return 管理员登录请求
     */
    public static LoginRequest buildAdminLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);
        return request;
    }
    
    /**
     * 生成测试JWT令牌（模拟）
     * @return 测试令牌
     */
    public static String generateTestJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQyNjIyfQ.test-signature";
    }
    
    /**
     * 生成过期的测试JWT令牌（模拟）
     * @return 过期令牌
     */
    public static String generateExpiredTestJwtToken() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.expired-signature";
    }
    
    /**
     * 生成无效的测试JWT令牌（模拟）
     * @return 无效令牌
     */
    public static String generateInvalidTestJwtToken() {
        return "invalid.jwt.token";
    }
    
    /**
     * 创建测试用户详情对象
     * @param username 用户名
     * @param password 密码
     * @return UserDetails对象
     */
    public static UserDetails buildTestUserDetails(String username, String password) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(password)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
    
    /**
     * 创建默认测试用户详情对象
     * @return UserDetails对象
     */
    public static UserDetails buildDefaultTestUserDetails() {
        return buildTestUserDetails(TEST_USERNAME, TEST_PASSWORD);
    }
}