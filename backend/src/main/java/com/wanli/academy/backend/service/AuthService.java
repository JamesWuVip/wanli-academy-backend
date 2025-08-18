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
 * 认证服务类
 * 处理用户注册、登录、令牌刷新等认证相关业务逻辑
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
        logger.info("开始用户注册流程，用户名: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("注册失败：用户名已存在 - {}", registerRequest.getUsername());
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("注册失败：邮箱已存在 - {}", registerRequest.getEmail());
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setIsActive(true);
        
        // 分配默认角色（ROLE_STUDENT）
        Optional<Role> studentRole = roleRepository.findByName("ROLE_STUDENT");
        if (studentRole.isPresent()) {
            user.addRole(studentRole.get());
        } else {
            logger.warn("默认角色 'ROLE_STUDENT' 不存在，创建新角色");
            Role newStudentRole = new Role("ROLE_STUDENT", "学生");
            newStudentRole.setIsActive(true);
            roleRepository.save(newStudentRole);
            user.addRole(newStudentRole);
        }
        
        // 保存用户
        User savedUser = userRepository.save(user);
        logger.info("用户注册成功，用户ID: {}", savedUser.getId());
        
        // 生成JWT令牌
        return generateAuthResponse(savedUser);
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 认证响应
     * @throws RuntimeException 当认证失败时抛出异常
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("开始用户登录流程，用户名或邮箱: {}", loginRequest.getUsernameOrEmail());
        
        try {
            // 进行身份验证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
                )
            );
            
            // 获取用户信息
            String usernameOrEmail = loginRequest.getUsernameOrEmail();
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查用户是否激活
            if (!user.getIsActive()) {
                logger.warn("登录失败：用户账户已被禁用 - {}", usernameOrEmail);
                throw new RuntimeException("用户账户已被禁用");
            }
            
            // 更新最后登录时间
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            logger.info("用户登录成功，用户ID: {}", user.getId());
            
            // 生成JWT令牌
            return generateAuthResponse(user);
            
        } catch (AuthenticationException e) {
            logger.error("登录失败：认证异常 - {}", e.getMessage());
            throw new RuntimeException("用户名或密码错误");
        }
    }
    
    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的认证响应
     * @throws RuntimeException 当刷新令牌无效时抛出异常
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("开始刷新令牌流程");
        
        try {
            // 验证刷新令牌
            if (!jwtService.isTokenValid(refreshToken)) {
                logger.warn("刷新令牌无效或已过期");
                throw new RuntimeException("刷新令牌无效或已过期");
            }
            
            // 从令牌中提取用户名
            String username = jwtService.extractUsername(refreshToken);
            
            // 获取用户信息
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查用户是否激活
            if (!user.getIsActive()) {
                logger.warn("刷新令牌失败：用户账户已被禁用 - {}", username);
                throw new RuntimeException("用户账户已被禁用");
            }
            
            logger.info("令牌刷新成功，用户ID: {}", user.getId());
            
            // 生成新的JWT令牌
            return generateAuthResponse(user);
            
        } catch (Exception e) {
            logger.error("刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("刷新令牌失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成认证响应
     * @param user 用户实体
     * @return 认证响应
     */
    private AuthResponse generateAuthResponse(User user) {
        // 使用CustomUserDetailsService获取包含角色信息的UserDetails
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        
        // 生成包含角色信息的JWT令牌
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
     * 检查用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        // 在事务内初始化懒加载的roles关联，避免LazyInitializationException
        userOptional.ifPresent(user -> user.getRoles().size());
        return userOptional;
    }
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        // 在事务内初始化懒加载的roles关联，避免LazyInitializationException
        userOptional.ifPresent(user -> user.getRoles().size());
        return userOptional;
    }
    
    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.info("获取所有用户列表");
        List<User> users = userRepository.findAll();
        // 在事务内初始化懒加载的roles关联，避免LazyInitializationException
        users.forEach(user -> user.getRoles().size());
        return users;
    }
}