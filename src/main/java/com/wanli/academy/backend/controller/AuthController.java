package com.wanli.academy.backend.controller;

import com.wanli.academy.backend.dto.AuthResponse;
import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.dto.RefreshTokenRequest;
import com.wanli.academy.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication Controller
 * Handles user registration, login, token refresh and other authentication-related HTTP requests
 */
@Tag(name = "User Authentication", description = "User authentication related API endpoints, including registration, login, token refresh, etc.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    /**
     * User Registration
     * POST /api/auth/register
     * 
     * @param registerRequest registration request containing username, email, password and other information
     * @param bindingResult validation result
     * @return registration response containing user information and JWT token
     */
    @Operation(
        summary = "User Registration",
        description = "Create new user account, requires username, email, password and other information. Returns user information and access token upon successful registration."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Registration successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters or username/email already exists"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Username or email already in use"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Parameter(description = "User registration request information", required = true)
            @Valid @RequestBody RegisterRequest registerRequest,
                                    BindingResult bindingResult) {
        logger.info("Received user registration request, username: {}", registerRequest.getUsername());
        
        // Check request parameter validation results
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registration successful");
            response.put("data", authResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("User registration successful, username: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            logger.error("User registration failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * User Login
     * POST /api/auth/login
     * 
     * @param loginRequest login request containing username and password
     * @return login response containing user information and JWT token
     */
    @Operation(
        summary = "User Login",
        description = "User authentication using username and password. Returns user information and access token upon successful login."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Incorrect username or password"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "User login request information", required = true)
            @Valid @RequestBody LoginRequest loginRequest,
                                 BindingResult bindingResult,
                                 HttpServletRequest request) {
        logger.info("Received user login request, username or email: {}", loginRequest.getUsernameOrEmail());
        logger.info("Request Content-Type: {}", request.getContentType());
        logger.info("LoginRequest object: usernameOrEmail={}, password={}", 
                   loginRequest.getUsernameOrEmail(), 
                   loginRequest.getPassword() != null ? "[SET]" : "[NOT SET]");
        
        // Check request parameter validation results
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("data", authResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("User login successful, username or email: {}", loginRequest.getUsernameOrEmail());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("User login failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    /**
     * Refresh Access Token
     * POST /api/auth/refresh
     * 
     * @param refreshTokenRequest refresh token request containing refresh token
     * @return new access token and refresh token
     */
    @Operation(
        summary = "Refresh Access Token",
        description = "Use refresh token to get new access token. Use this interface when access token expires."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refresh successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @Parameter(description = "Refresh token request information", required = true)
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
                                        BindingResult bindingResult) {
        logger.info("Received refresh token request");
        
        // Check request parameter validation results
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            AuthResponse authResponse = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token refresh successful");
            response.put("data", authResponse);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Token refresh successful");
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    /**
     * Check if username is available
     * @param username username
     * @return availability check result
     */
    @Operation(
        summary = "Check Username Availability",
        description = "Check if the specified username is already in use. Used for username validation before registration."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Check completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        )
    })
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(
            @Parameter(description = "Username", required = true)
            @RequestParam String username) {
        logger.info("Checking username availability: {}", username);
        
        if (username == null || username.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Username cannot be empty");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean available = authService.isUsernameAvailable(username.trim());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", available);
        response.put("message", available ? "Username is available" : "Username already exists");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if email is available
     * GET /api/auth/check-email
     * 
     * @param email email address
     * @return availability check result
     */
    @Operation(
        summary = "Check Email Availability",
        description = "Check if the specified email address is already in use. Used for email validation before registration."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Check completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        )
    })
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(
            @Parameter(description = "Email address", required = true)
            @RequestParam String email) {
        logger.info("Checking email availability: {}", email);
        
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Email cannot be empty");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean available = authService.isEmailAvailable(email.trim());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("available", available);
        response.put("message", available ? "Email is available" : "Email already exists");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * GET /api/auth/health
     * 
     * @return service status information
     */
    @Operation(
        summary = "Health Check",
        description = "Check the health status of the authentication service"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Object.class)
            )
        )
    })
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "healthy");
        response.put("message", "Authentication service is running normally");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "auth-service");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create validation error response
     * @param bindingResult validation result
     * @return error response Map
     */
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Request parameter validation failed");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        // Collect all validation errors
        Map<String, String> fieldErrors = bindingResult.getFieldErrors().stream()
            .collect(Collectors.toMap(
                fieldError -> fieldError.getField(),
                fieldError -> fieldError.getDefaultMessage(),
                (existing, replacement) -> existing // If there are duplicate fields, keep the first error message
            ));
        
        errorResponse.put("errors", fieldErrors);
        
        return errorResponse;
    }
    
    /**
     * Global exception handling
     * @param e exception
     * @return error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        logger.error("Unhandled exception in authentication controller: ", e);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Internal server error");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
