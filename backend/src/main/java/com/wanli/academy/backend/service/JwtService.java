package com.wanli.academy.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT服务类
 * 负责JWT令牌的生成、解析和验证
 */
@Service
public class JwtService {

    // JWT密钥，应该从配置文件中读取
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    // JWT过期时间（毫秒），默认24小时
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // 刷新令牌过期时间（毫秒），默认7天
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    /**
     * 从JWT令牌中提取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中提取过期时间
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT令牌中提取指定声明
     * @param token JWT令牌
     * @param claimsResolver 声明解析器
     * @param <T> 声明类型
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 生成JWT令牌
     * @param userDetails 用户详情
     * @return JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * 生成JWT令牌（通过用户名）
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateTokenFromUsername(String username) {
        Map<String, Object> extraClaims = new HashMap<>();
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成JWT令牌（通过用户名）- 别名方法
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateTokenByUsername(String username) {
        return generateTokenFromUsername(username);
    }

    /**
     * 生成带有额外声明的JWT令牌
     * @param extraClaims 额外声明
     * @param userDetails 用户详情
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * 生成刷新令牌
     * @param userDetails 用户详情
     * @return 刷新令牌
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * 生成刷新令牌（通过用户名）
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshTokenFromUsername(String username) {
        Map<String, Object> extraClaims = new HashMap<>();
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌（通过用户名）- 别名方法
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshTokenByUsername(String username) {
        return generateRefreshTokenFromUsername(username);
    }

    /**
     * 构建JWT令牌
     * @param extraClaims 额外声明
     * @param userDetails 用户详情
     * @param expiration 过期时间
     * @return JWT令牌
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * 验证JWT令牌是否有效（仅检查令牌格式和过期时间）
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查JWT令牌是否过期
     * @param token JWT令牌
     * @return 是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 从JWT令牌中提取所有声明
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取签名密钥
     * @return 签名密钥
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取JWT过期时间（毫秒）
     * @return JWT过期时间
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * 获取刷新令牌过期时间（毫秒）
     * @return 刷新令牌过期时间
     */
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    /**
     * 从JWT令牌中提取用户ID（如果存在）
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        return null;
    }

    /**
     * 生成包含用户ID的JWT令牌
     * @param userDetails 用户详情
     * @param userId 用户ID
     * @return JWT令牌
     */
    public String generateTokenWithUserId(UserDetails userDetails, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        return generateToken(extraClaims, userDetails);
    }
}