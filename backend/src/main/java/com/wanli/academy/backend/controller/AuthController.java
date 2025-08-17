package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.AuthResponse;
import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RefreshTokenRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 处理用户注册、登录、令牌刷新等认证相关的HTTP请求
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @param bindingResult 验证结果
     * @return 认证响应或错误信息
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest,
                                    BindingResult bindingResult) {
        logger.info("收到用户注册请求，用户名: {}", registerRequest.getUsername());
        
        // 检查请求参数验证结果
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户注册成功");
            response.put("data", authResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("用户注册成功，用户名: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("用户注册失败: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @param bindingResult 验证结果
     * @return 认证响应或错误信息
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                 BindingResult bindingResult,
                                 HttpServletRequest request) {
        logger.info("收到用户登录请求，用户名或邮箱: {}", loginRequest.getUsernameOrEmail());
        logger.info("请求Content-Type: {}", request.getContentType());
        logger.info("LoginRequest对象: usernameOrEmail={}, password={}", 
                   loginRequest.getUsernameOrEmail(), 
                   loginRequest.getPassword() != null ? "[已设置]" : "[未设置]");
        
        // 检查请求参数验证结果
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", authResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("用户登录成功，用户名或邮箱: {}", loginRequest.getUsernameOrEmail());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("用户登录失败: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    /**
     * 刷新访问令牌
     * @param refreshTokenRequest 刷新令牌请求
     * @param bindingResult 验证结果
     * @return 新的认证响应或错误信息
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
                                        BindingResult bindingResult) {
        logger.info("收到刷新令牌请求");
        
        // 检查请求参数验证结果
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "令牌刷新成功");
            response.put("data", authResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("令牌刷新成功");
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("令牌刷新失败: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 可用性检查结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        logger.info("检查用户名可用性: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名不能为空");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean available = authService.isUsernameAvailable(username.trim());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", available);
        response.put("message", available ? "用户名可用" : "用户名已存在");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 可用性检查结果
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        logger.info("检查邮箱可用性: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "邮箱不能为空");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean available = authService.isEmailAvailable(email.trim());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", available);
        response.put("message", available ? "邮箱可用" : "邮箱已存在");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 健康检查端点
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "认证服务运行正常");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "auth-service");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 创建验证错误响应
     * @param bindingResult 验证结果
     * @return 错误响应Map
     */
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "请求参数验证失败");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        // 收集所有验证错误
        Map<String, String> fieldErrors = bindingResult.getFieldErrors().stream()
            .collect(Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage(),
                (existing, replacement) -> existing // 如果有重复的字段，保留第一个错误信息
            ));
        
        errorResponse.put("errors", fieldErrors);
        
        return errorResponse;
    }
    
    /**
     * 全局异常处理
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("认证控制器发生未处理异常: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "服务器内部错误");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}