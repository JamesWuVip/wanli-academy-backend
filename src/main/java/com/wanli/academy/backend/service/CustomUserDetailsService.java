package com.wanli.academy.backend.service;

import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Spring Security User Details Service Implementation
 * Responsible for loading user details based on username or user ID
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Load user details by username (Spring Security interface method)
     * @param username username
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        
        logger.debug("Successfully loaded user: {} with role: {}", username, user.getRole());
        return buildUserDetails(user);
    }
    
    /**
     * Load user details by user ID
     * @param userId user ID
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });
        
        logger.debug("Successfully loaded user: {} with role: {}", user.getUsername(), user.getRole());
        return buildUserDetails(user);
    }
    
    /**
     * Build Spring Security UserDetails object
     * @param user user entity
     * @return UserDetails object
     */
    private UserDetails buildUserDetails(User user) {
        // Check if user is active
        boolean isActive = user.getIsActive() != null ? user.getIsActive() : true;
        
        if (!isActive) {
            logger.warn("User {} is inactive", user.getUsername());
            throw new UsernameNotFoundException("User account is inactive: " + user.getUsername());
        }
        
        // Get user authorities (roles)
        Collection<GrantedAuthority> authorities = getUserAuthorities(user);
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!isActive)
                .build();
    }
    
    /**
     * Get user authorities based on role
     * @param user user entity
     * @return collection of granted authorities
     */
    private Collection<GrantedAuthority> getUserAuthorities(User user) {
        String role = user.getRole();
        
        // Ensure role has ROLE_ prefix
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        
        logger.debug("User {} has role: {}", user.getUsername(), role);
        
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        } else {
            logger.warn("User {} has no role assigned, defaulting to ROLE_STUDENT", user.getUsername());
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"));
        }
    }
}