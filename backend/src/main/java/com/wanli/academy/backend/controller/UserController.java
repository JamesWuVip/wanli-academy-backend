package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.UserResponse;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * 获取当前登录用户信息
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        logger.info("收到获取当前用户信息请求");
        
        try {
            // 从安全上下文中获取当前认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("用户未认证");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户未认证");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // 获取用户名
            String username = authentication.getName();
            logger.info("获取用户信息，用户名: {}", username);
            
            // 从数据库获取用户详细信息
            Optional<User> userOptional = authService.getUserByUsername(username);
            
            if (userOptional.isEmpty()) {
                logger.error("用户不存在: {}", username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            User user = userOptional.get();
            
            // 检查用户是否激活
            if (!user.getIsActive()) {
                logger.warn("用户账户已被禁用: {}", username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户账户已被禁用");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // 转换为UserResponse DTO
            UserResponse userResponse = convertToUserResponse(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "获取用户信息成功");
            response.put("data", userResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("成功返回用户信息，用户ID: {}", user.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取当前用户信息失败: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取用户信息失败");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 获取用户个人资料（与/me相同，提供备用端点）
     * @return 当前用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        logger.info("收到获取用户个人资料请求");
        return getCurrentUser();
    }
    
    /**
     * 检查用户认证状态
     * @return 认证状态信息
     */
    @GetMapping("/auth-status")
    public ResponseEntity<?> getAuthStatus() {
        logger.info("收到检查认证状态请求");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            boolean isAuthenticated = authentication != null && 
                                    authentication.isAuthenticated() && 
                                    !"anonymousUser".equals(authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("authenticated", isAuthenticated);
            response.put("timestamp", LocalDateTime.now());
            
            if (isAuthenticated) {
                response.put("username", authentication.getName());
                response.put("authorities", authentication.getAuthorities());
                response.put("message", "用户已认证");
            } else {
                response.put("message", "用户未认证");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("检查认证状态失败: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查认证状态失败");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 将User实体转换为UserResponse DTO
     * @param user 用户实体
     * @return UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        // 获取用户角色名称集合
        Set<String> roleNames = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
        
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getIsActive(),
            roleNames,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
    
    /**
     * 全局异常处理
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("用户控制器发生未处理异常: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "服务器内部错误");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}