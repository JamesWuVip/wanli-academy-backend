package com.wanli.academy.backend.service;

import com.wanli.academy.backend.util.TestDataUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT服务测试")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private UserDetails testUserDetails;
    private String testUsername;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUsername = "testuser";
        testUserDetails = TestDataUtil.buildTestUserDetails(testUsername, "password");
        validToken = jwtService.generateToken(testUserDetails);
    }

    @Nested
    @DisplayName("令牌生成测试")
    class TokenGenerationTests {
        
        @Test
        @DisplayName("应该生成有效的JWT令牌")
        void shouldGenerateValidToken() {
            // When
            String token = jwtService.generateToken(testUserDetails);
            
            // Then
            assertNotNull(token, "生成的令牌不应为空");
            assertFalse(token.isEmpty(), "生成的令牌不应为空字符串");
            assertTrue(token.contains("."), "JWT令牌应包含点分隔符");
        }
        
        @Test
        @DisplayName("应该生成包含额外声明的令牌")
        void shouldGenerateTokenWithExtraClaims() {
            // Given
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", "USER");
            extraClaims.put("department", "IT");
            
            // When
            String token = jwtService.generateToken(extraClaims, testUserDetails);
            
            // Then
            assertNotNull(token, "生成的令牌不应为空");
            
            // 通过公共方法间接验证额外声明
            String extractedUsername = jwtService.extractUsername(token);
            assertEquals(testUsername, extractedUsername, "令牌中的用户名应该正确");
            
            boolean isValid = jwtService.isTokenValid(token, testUserDetails);
            assertTrue(isValid, "包含额外声明的令牌应该有效");
        }
        
        @Test
        @DisplayName("应该生成刷新令牌")
        void shouldGenerateRefreshToken() {
            // When
            String refreshToken = jwtService.generateRefreshToken(testUserDetails);
            
            // Then
            assertNotNull(refreshToken, "生成的刷新令牌不应为空");
            assertFalse(refreshToken.isEmpty(), "生成的刷新令牌不应为空字符串");
            assertTrue(refreshToken.contains("."), "刷新令牌应包含点分隔符");
        }
    }

    @Nested
    @DisplayName("令牌解析测试")
    class TokenExtractionTests {
        
        @Test
        @DisplayName("应该正确提取用户名")
        void shouldExtractUsername() {
            // When
            String extractedUsername = jwtService.extractUsername(validToken);
            
            // Then
            assertEquals(testUsername, extractedUsername, "应该正确提取用户名");
        }
        
        @Test
        @DisplayName("应该正确提取过期时间")
        void shouldExtractExpiration() {
            // When
            Date expiration = jwtService.extractExpiration(validToken);
            
            // Then
            assertNotNull(expiration, "过期时间不应为空");
            assertTrue(expiration.after(new Date()), "过期时间应该在未来");
        }
    }

    @Nested
    @DisplayName("令牌验证测试")
    class TokenValidationTests {
        
        @Test
        @DisplayName("应该验证有效令牌")
        void shouldValidateValidToken() {
            // When
            boolean isValid = jwtService.isTokenValid(validToken, testUserDetails);
            
            // Then
            assertTrue(isValid, "有效令牌应该通过验证");
        }
        
        @Test
        @DisplayName("应该拒绝用户名不匹配的令牌")
        void shouldRejectTokenWithWrongUsername() {
            // Given
            UserDetails wrongUserDetails = TestDataUtil.buildTestUserDetails("wronguser", "password");
            
            // When
            boolean isValid = jwtService.isTokenValid(validToken, wrongUserDetails);
            
            // Then
            assertFalse(isValid, "用户名不匹配的令牌应该被拒绝");
        }
        
        @Test
        @DisplayName("应该正确识别无效令牌")
        void shouldIdentifyInvalidToken() {
            // Given - 使用无效令牌
            String invalidToken = "invalid.jwt.token";
            
            // When & Then - 测试无效令牌验证
            assertThrows(Exception.class, () -> {
                jwtService.isTokenValid(invalidToken, testUserDetails);
            }, "无效令牌应该抛出异常");
        }
    }

    @Nested
    @DisplayName("令牌刷新测试")
    class TokenRefreshTests {
        
        @Test
        @DisplayName("应该使用用户名生成刷新令牌")
        void shouldGenerateRefreshTokenWithUsername() {
            // When
            String refreshToken = jwtService.generateRefreshTokenByUsername(testUsername);
            
            // Then
            assertNotNull(refreshToken, "刷新令牌不应为空");
            assertFalse(refreshToken.isEmpty(), "刷新令牌不应为空字符串");
        }
        
        @Test
        @DisplayName("应该验证令牌格式和过期时间")
        void shouldValidateTokenFormatAndExpiration() {
            // When
            boolean isValid = jwtService.isTokenValid(validToken);
            
            // Then
            assertTrue(isValid, "有效令牌应该通过验证");
        }
        
        @Test
        @DisplayName("应该拒绝格式错误的令牌")
        void shouldRejectMalformedToken() {
            // Given
            String malformedToken = "invalid.token.format";
            
            // When
            boolean isValid = jwtService.isTokenValid(malformedToken);
            
            // Then
            assertFalse(isValid, "格式错误的令牌应该被拒绝");
        }
    }

    @Nested
    @DisplayName("用户名令牌生成测试")
    class UsernameTokenGenerationTests {
        
        @Test
        @DisplayName("应该通过用户名生成访问令牌")
        void should_generateAccessToken_when_usernameProvided() {
            // When
            String token = jwtService.generateTokenFromUsername(testUsername);
            
            // Then
            assertNotNull(token, "生成的令牌不应为空");
            assertFalse(token.isEmpty(), "生成的令牌不应为空字符串");
            
            String extractedUsername = jwtService.extractUsername(token);
            assertEquals(testUsername, extractedUsername, "令牌中的用户名应该正确");
        }
        
        @Test
        @DisplayName("应该通过用户名生成访问令牌（别名方法）")
        void should_generateAccessToken_when_usernameProvidedAlias() {
            // When
            String token = jwtService.generateTokenByUsername(testUsername);
            
            // Then
            assertNotNull(token, "生成的令牌不应为空");
            assertFalse(token.isEmpty(), "生成的令牌不应为空字符串");
            
            String extractedUsername = jwtService.extractUsername(token);
            assertEquals(testUsername, extractedUsername, "令牌中的用户名应该正确");
        }
        
        @Test
        @DisplayName("应该通过用户名生成刷新令牌")
        void should_generateRefreshToken_when_usernameProvided() {
            // When
            String refreshToken = jwtService.generateRefreshTokenFromUsername(testUsername);
            
            // Then
            assertNotNull(refreshToken, "生成的刷新令牌不应为空");
            assertFalse(refreshToken.isEmpty(), "生成的刷新令牌不应为空字符串");
            
            String extractedUsername = jwtService.extractUsername(refreshToken);
            assertEquals(testUsername, extractedUsername, "刷新令牌中的用户名应该正确");
        }
    }

    @Nested
    @DisplayName("用户ID令牌测试")
    class UserIdTokenTests {
        
        @Test
        @DisplayName("应该生成包含用户ID的令牌")
        void should_generateTokenWithUserId_when_userIdProvided() {
            // Given
            Long userId = 123L;
            
            // When
            String token = jwtService.generateTokenWithUserId(testUserDetails, userId);
            
            // Then
            assertNotNull(token, "生成的令牌不应为空");
            assertFalse(token.isEmpty(), "生成的令牌不应为空字符串");
            
            Long extractedUserId = jwtService.extractUserId(token);
            assertEquals(userId, extractedUserId, "令牌中的用户ID应该正确");
        }
        
        @Test
        @DisplayName("应该从不包含用户ID的令牌中返回null")
        void should_returnNull_when_tokenDoesNotContainUserId() {
            // When
            Long extractedUserId = jwtService.extractUserId(validToken);
            
            // Then
            assertNull(extractedUserId, "不包含用户ID的令牌应该返回null");
        }
    }

    @Nested
    @DisplayName("配置属性测试")
    class ConfigurationTests {
        
        @Test
        @DisplayName("应该返回JWT过期时间")
        void should_returnJwtExpiration_when_requested() {
            // When
            long expiration = jwtService.getJwtExpiration();
            
            // Then
            assertTrue(expiration > 0, "JWT过期时间应该大于0");
        }
        
        @Test
        @DisplayName("应该返回刷新令牌过期时间")
        void should_returnRefreshExpiration_when_requested() {
            // When
            long refreshExpiration = jwtService.getRefreshExpiration();
            
            // Then
            assertTrue(refreshExpiration > 0, "刷新令牌过期时间应该大于0");
            assertTrue(refreshExpiration > jwtService.getJwtExpiration(), "刷新令牌过期时间应该大于访问令牌过期时间");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {
        
        @Test
        @DisplayName("应该处理空令牌")
        void should_handleNullToken_when_validating() {
            // When & Then
            assertThrows(Exception.class, () -> {
                jwtService.isTokenValid(null, testUserDetails);
            }, "空令牌应该抛出异常");
        }
        
        @Test
        @DisplayName("应该处理空用户名提取")
        void should_handleNullToken_when_extractingUsername() {
            // When & Then
            assertThrows(Exception.class, () -> {
                jwtService.extractUsername(null);
            }, "从空令牌提取用户名应该抛出异常");
        }
        
        @Test
        @DisplayName("应该处理空令牌的过期时间提取")
        void should_handleNullToken_when_extractingExpiration() {
            // When & Then
            assertThrows(Exception.class, () -> {
                jwtService.extractExpiration(null);
            }, "从空令牌提取过期时间应该抛出异常");
        }
    }
}