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
 * User Controller
 * Handles user-related HTTP requests
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * Get current logged-in user information
     * @return Current user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        logger.info("Received get current user information request");
        
        try {
            // Get current authentication information from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("User not authenticated");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not authenticated");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Get username
            String username = authentication.getName();
            logger.info("Getting user information, username: {}", username);
            
            // Get user details from database
            Optional<User> userOptional = authService.getUserByUsername(username);
            
            if (userOptional.isEmpty()) {
                logger.error("User not found: {}", username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            User user = userOptional.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                logger.warn("User account has been disabled: {}", username);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User account has been disabled");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // Convert to UserResponse DTO
            UserResponse userResponse = convertToUserResponse(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Get user information successful");
            response.put("data", userResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Successfully returned user information, user ID: {}", user.getId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get current user information: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get user information");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get user profile (same as /me, provides alternative endpoint)
     * @return Current user information
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        logger.info("Received get user profile request");
        return getCurrentUser();
    }
    
    /**
     * Get all users list (admin only)
     * @return User list
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        logger.info("Received get user list request");
        
        try {
            // Get current authentication information from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("User not authenticated");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not authenticated");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Check if user has admin or headquarters teacher role
            boolean hasPermission = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || 
                                auth.getAuthority().equals("ROLE_HQ_TEACHER"));
            
            if (!hasPermission) {
                logger.warn("User has insufficient permissions to access user list: {}", authentication.getName());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Insufficient permissions, only admins and headquarters teachers can access");
                errorResponse.put("timestamp", LocalDateTime.now());
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // Get all users
            var users = authService.getAllUsers();
            var userResponses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Get user list successful");
            response.put("data", userResponses);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Successfully returned user list, total {} users", userResponses.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get user list: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get user list");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Check user authentication status
     * @return Authentication status information
     */
    @GetMapping("/auth-status")
    public ResponseEntity<?> getAuthStatus() {
        logger.info("Received check authentication status request");
        
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
                response.put("message", "User authenticated");
            } else {
                response.put("message", "User not authenticated");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to check authentication status: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to check authentication status");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Convert User entity to UserResponse DTO
     * @param user User entity
     * @return UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        // Get user role name collection
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
     * Global exception handling
     * @param e Exception
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("Unhandled exception occurred in user controller: {}", e.getMessage(), e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Internal server error");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}