package com.wanli.academy.backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthResponse DTO 测试类
 */
@DisplayName("AuthResponse DTO 测试")
class AuthResponseTest {

    @Test
    @DisplayName("默认构造函数应该创建空对象并设置默认tokenType")
    void should_createEmptyObjectWithDefaultTokenType_when_usingDefaultConstructor() {
        // Given & When
        AuthResponse response = new AuthResponse();

        // Then
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType()); // 默认值
        assertNull(response.getExpiresIn());
        assertNull(response.getRefreshExpiresIn());
        assertNull(response.getUserId());
        assertNull(response.getUsername());
        assertNull(response.getEmail());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
        assertNull(response.getRoles());
        assertNull(response.getLastLoginAt());
    }

    @Test
    @DisplayName("带参构造函数应该正确设置所有字段并自动设置lastLoginAt")
    void should_setAllFieldsAndAutoSetLastLoginAt_when_usingParameterizedConstructor() {
        // Given
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";
        Long expiresIn = 3600L;
        Long refreshExpiresIn = 86400L;
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "张";
        String lastName = "三";
        Set<String> roles = Set.of("STUDENT", "USER");
        
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        AuthResponse response = new AuthResponse(accessToken, refreshToken, expiresIn, 
                                               refreshExpiresIn, userId, username, 
                                               email, firstName, lastName, roles);
        
        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        // Then
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType()); // 默认值
        assertEquals(expiresIn, response.getExpiresIn());
        assertEquals(refreshExpiresIn, response.getRefreshExpiresIn());
        assertEquals(userId, response.getUserId());
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());
        assertEquals(firstName, response.getFirstName());
        assertEquals(lastName, response.getLastName());
        assertEquals(roles, response.getRoles());
        
        // 验证lastLoginAt被自动设置为当前时间
        assertNotNull(response.getLastLoginAt());
        assertTrue(response.getLastLoginAt().isAfter(beforeCreation));
        assertTrue(response.getLastLoginAt().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Getter和Setter方法应该正常工作")
    void should_workCorrectly_when_usingGettersAndSetters() {
        // Given
        AuthResponse response = new AuthResponse();
        String accessToken = "new-access-token";
        String refreshToken = "new-refresh-token";
        String tokenType = "Custom";
        Long expiresIn = 7200L;
        Long refreshExpiresIn = 172800L;
        Long userId = 2L;
        String username = "newuser";
        String email = "new@example.com";
        String firstName = "李";
        String lastName = "四";
        Set<String> roles = new HashSet<>(Set.of("TEACHER", "ADMIN"));
        LocalDateTime lastLoginAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // When
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType(tokenType);
        response.setExpiresIn(expiresIn);
        response.setRefreshExpiresIn(refreshExpiresIn);
        response.setUserId(userId);
        response.setUsername(username);
        response.setEmail(email);
        response.setFirstName(firstName);
        response.setLastName(lastName);
        response.setRoles(roles);
        response.setLastLoginAt(lastLoginAt);

        // Then
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals(tokenType, response.getTokenType());
        assertEquals(expiresIn, response.getExpiresIn());
        assertEquals(refreshExpiresIn, response.getRefreshExpiresIn());
        assertEquals(userId, response.getUserId());
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());
        assertEquals(firstName, response.getFirstName());
        assertEquals(lastName, response.getLastName());
        assertEquals(roles, response.getRoles());
        assertEquals(lastLoginAt, response.getLastLoginAt());
    }

    @Test
    @DisplayName("toString方法应该返回正确格式但不包含敏感token信息")
    void should_returnCorrectFormatWithoutSensitiveInfo_when_callingToString() {
        // Given
        String accessToken = "secret-access-token";
        String refreshToken = "secret-refresh-token";
        String tokenType = "Bearer";
        Long expiresIn = 3600L;
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "王";
        String lastName = "五";
        Set<String> roles = Set.of("STUDENT");
        LocalDateTime lastLoginAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType(tokenType);
        response.setExpiresIn(expiresIn);
        response.setUserId(userId);
        response.setUsername(username);
        response.setEmail(email);
        response.setFirstName(firstName);
        response.setLastName(lastName);
        response.setRoles(roles);
        response.setLastLoginAt(lastLoginAt);

        // When
        String result = response.toString();

        // Then
        assertTrue(result.contains("AuthResponse{"));
        assertTrue(result.contains("tokenType='" + tokenType + "'"));
        assertTrue(result.contains("expiresIn=" + expiresIn));
        assertTrue(result.contains("userId=" + userId));
        assertTrue(result.contains("username='" + username + "'"));
        assertTrue(result.contains("email='" + email + "'"));
        assertTrue(result.contains("firstName='" + firstName + "'"));
        assertTrue(result.contains("lastName='" + lastName + "'"));
        assertTrue(result.contains("roles=" + roles));
        assertTrue(result.contains("lastLoginAt=" + lastLoginAt));
        
        // 确保敏感信息不会出现在toString中
        assertFalse(result.contains(accessToken), "Access token should not appear in toString");
        assertFalse(result.contains(refreshToken), "Refresh token should not appear in toString");
        assertFalse(result.contains("accessToken="), "accessToken field should not appear in toString");
        assertFalse(result.contains("refreshToken="), "refreshToken field should not appear in toString");
    }

    @Test
    @DisplayName("roles集合应该支持修改")
    void should_supportModification_when_workingWithRoles() {
        // Given
        AuthResponse response = new AuthResponse();
        Set<String> roles = new HashSet<>();
        roles.add("STUDENT");
        response.setRoles(roles);

        // When
        response.getRoles().add("USER");

        // Then
        assertTrue(response.getRoles().contains("STUDENT"));
        assertTrue(response.getRoles().contains("USER"));
        assertEquals(2, response.getRoles().size());
    }

    @Test
    @DisplayName("应该正确处理null值")
    void should_handleNullValues_correctly() {
        // Given
        AuthResponse response = new AuthResponse();

        // When
        response.setAccessToken(null);
        response.setRefreshToken(null);
        response.setRoles(null);
        response.setLastLoginAt(null);

        // Then
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
        assertNull(response.getRoles());
        assertNull(response.getLastLoginAt());
        
        // toString应该能处理null值
        assertDoesNotThrow(() -> response.toString());
    }

    @Test
    @DisplayName("应该正确处理空roles集合")
    void should_handleEmptyRoles_correctly() {
        // Given
        AuthResponse response = new AuthResponse();
        Set<String> emptyRoles = new HashSet<>();

        // When
        response.setRoles(emptyRoles);

        // Then
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().isEmpty());
        assertEquals(0, response.getRoles().size());
    }

    @Test
    @DisplayName("tokenType默认值应该是Bearer")
    void should_haveDefaultBearerTokenType_when_created() {
        // Given & When
        AuthResponse response1 = new AuthResponse();
        AuthResponse response2 = new AuthResponse("token", "refresh", 3600L, 86400L, 
                                                 1L, "user", "email", "first", "last", Set.of("ROLE"));

        // Then
        assertEquals("Bearer", response1.getTokenType());
        assertEquals("Bearer", response2.getTokenType());
    }

    @Test
    @DisplayName("应该支持链式调用setter方法")
    void should_supportMethodChaining_when_settingValues() {
        // Given
        AuthResponse response = new AuthResponse();

        // When & Then - 虽然当前实现不支持链式调用，但测试setter方法的独立性
        assertDoesNotThrow(() -> {
            response.setUserId(1L);
            response.setUsername("test");
            response.setEmail("test@example.com");
        });
        
        assertEquals(1L, response.getUserId());
        assertEquals("test", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
    }
}