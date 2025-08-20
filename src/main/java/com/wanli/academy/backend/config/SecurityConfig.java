package com.wanli.academy.backend.config;

import com.wanli.academy.backend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration Class
 * Configures authentication, authorization, CORS and other security settings
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Password Encoder Bean
     * Uses BCrypt algorithm for password encryption
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * DAO Authentication Provider Bean
     * Configures user details service and password encoder
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * CORS Configuration
     * Allows frontend cross-origin access
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Allow all origins
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Preflight request cache time
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * HTTP Security Configuration
     * Configures request authorization rules and JWT filter
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection (using JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management as stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure exception handling
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Configure request authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow access to health check endpoints
                .requestMatchers("/actuator/health").permitAll()
                
                // Allow access to Actuator monitoring endpoints (for diagnostics)
                .requestMatchers("/actuator/**").permitAll()
                
                // Allow access to Swagger documentation (if needed)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Allow access to static resources
                .requestMatchers("/static/**", "/public/**").permitAll()
                
                // Admin permissions - full access
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                
                // Teacher permissions - homework and submission management
                .requestMatchers("/api/homework/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER")
                .requestMatchers("/api/homeworks/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER")
                
                // Assignment related permissions - let @PreAuthorize annotation handle specific permission control
                .requestMatchers("/api/assignments/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                
                .requestMatchers("/api/submissions/*/grade").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER")
                .requestMatchers("/api/submissions/assignment/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER")
                .requestMatchers("/api/submissions/create").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/submissions/my").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/submissions/my-submissions").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/submissions/*/result").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                
                // File management permissions
                .requestMatchers("/api/files/upload").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/files/download/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/files/delete/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/files/cleanup").hasAuthority("ROLE_ADMIN")
                
                // User information access
                .requestMatchers("/api/users/me").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/users/profile").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/users/auth-status").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER", "ROLE_FRANCHISE_TEACHER", "ROLE_STUDENT")
                .requestMatchers("/api/users").hasAnyAuthority("ROLE_ADMIN", "ROLE_HQ_TEACHER")
                .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}