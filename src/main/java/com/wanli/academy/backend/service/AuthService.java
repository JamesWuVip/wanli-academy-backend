package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.AuthResponse;
import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.RoleRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication Service
 * Handles user registration, login, token refresh and other authentication related business logic
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 认证响应
     * @throws RuntimeException 当用户名或邮箱已存在时抛出异常
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Starting user registration process, username: {}", registerRequest.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: username already exists - {}", registerRequest.getUsername());
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: email already exists - {}", registerRequest.getEmail());
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setIsActive(true);
        
        // Assign default role (ROLE_STUDENT)
        Optional<Role> studentRole = roleRepository.findByName("ROLE_STUDENT");
        if (studentRole.isPresent()) {
            user.addRole(studentRole.get());
        } else {
            logger.warn("Default role 'ROLE_STUDENT' does not exist, creating new role");
            Role newStudentRole = new Role("ROLE_STUDENT", "Student");
            newStudentRole.setIsActive(true);
            roleRepository.save(newStudentRole);
            user.addRole(newStudentRole);
        }
        
        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User registration successful, user ID: {}", savedUser.getId());
        
        // Generate JWT token
        return generateAuthResponse(savedUser);
    }
    
    /**
     * User login
     * @param loginRequest login request
     * @return authentication response
     * @throws RuntimeException when authentication fails
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Starting user login process, username or email: {}", loginRequest.getUsernameOrEmail());
        
        try {
            // Perform authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );
            
            // Get user information
            String usernameOrEmail = loginRequest.getUsernameOrEmail();
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user is active
            if (!user.getIsActive()) {
                logger.warn("Login failed: user account is disabled - {}", usernameOrEmail);
                throw new RuntimeException("User account is disabled");
            }
            
            // Update last login time
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            logger.info("User login successful, user ID: {}", user.getId());
            
            // Generate JWT token
            return generateAuthResponse(user);
            
        } catch (AuthenticationException e) {
            logger.error("Login failed: authentication exception - {}", e.getMessage());
            throw new RuntimeException("Invalid username or password");
        }
    }
    
    /**
     * Refresh access token
     * @param refreshToken refresh token
     * @return new authentication response
     * @throws RuntimeException when refresh token is invalid
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Starting token refresh process");
        
        try {
            // Validate refresh token
            if (!jwtService.isTokenValid(refreshToken)) {
                logger.warn("Refresh token is invalid or expired");
                throw new RuntimeException("Refresh token is invalid or expired");
            }
            
            // Extract username from token
            String username = jwtService.extractUsername(refreshToken);
            
            // Get user information
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user is active
            if (!user.getIsActive()) {
                logger.warn("Token refresh failed: user account is disabled - {}", username);
                throw new RuntimeException("User account is disabled");
            }
            
            logger.info("Token refresh successful, user ID: {}", user.getId());
            
            // Generate new JWT token
            return generateAuthResponse(user);
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate authentication response
     * @param user user entity
     * @return authentication response
     */
    private AuthResponse generateAuthResponse(User user) {
        // Use CustomUserDetailsService to get UserDetails with role information
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        
        // Generate JWT token with role information
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtService.getJwtExpiration());
        response.setRefreshExpiresIn(jwtService.getRefreshExpiration());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRoles(roles);
        response.setLastLoginAt(LocalDateTime.now());
        
        return response;
    }
    
    /**
     * Check if username is available
     * @param username username
     * @return whether available
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email is available
     * @param email email
     * @return whether available
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    /**
     * Get user information by username
     * @param username username
     * @return user entity
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        // Initialize lazy-loaded roles association within transaction to avoid LazyInitializationException
        userOptional.ifPresent(user -> user.getRoles().size());
        return userOptional;
    }
    
    /**
     * Get user information by user ID
     * @param userId user ID
     * @return user entity
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        // Initialize lazy-loaded roles association within transaction to avoid LazyInitializationException
        userOptional.ifPresent(user -> user.getRoles().size());
        return userOptional;
    }
    
    /**
     * Get all users list
     * @return users list
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.info("Getting all users list");
        List<User> users = userRepository.findAll();
        // Initialize lazy-loaded roles association within transaction to avoid LazyInitializationException
        users.forEach(user -> user.getRoles().size());
        return users;
    }
}
