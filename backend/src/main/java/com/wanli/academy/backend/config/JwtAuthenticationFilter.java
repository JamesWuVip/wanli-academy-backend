package com.wanli.academy.backend.config;

import com.wanli.academy.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT令牌并验证用户身份
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // 从请求头中获取JWT令牌
        String jwt = getTokenFromRequest(request);
        
        // 如果令牌存在且用户未认证
        if (StringUtils.hasText(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 从JWT中提取用户名
                String username = jwtService.extractUsername(jwt);
                
                if (StringUtils.hasText(username)) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 验证JWT令牌
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        // 创建认证令牌
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        // 设置认证详情
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 将认证信息设置到安全上下文中
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // JWT验证失败，记录日志但不抛出异常
                logger.error("JWT令牌验证失败: " + e.getMessage());
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT令牌
     * @param request HTTP请求
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 移除"Bearer "前缀
        }
        return null;
    }
}