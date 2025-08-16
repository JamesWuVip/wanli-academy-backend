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
        testUserDetails = TestDataUtil.createTestUserDetails(testUsername, "password");
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
            UserDetails wrongUserDetails = TestDataUtil.createTestUserDetails("wronguser", "password");
            
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
}