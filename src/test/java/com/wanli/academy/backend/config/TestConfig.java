package com.wanli.academy.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * 测试配置类
 * 提供测试环境所需的Bean配置
 */
@TestConfiguration
@ActiveProfiles("test")
public class TestConfig {
    
    /**
     * 测试环境密码编码器
     * 使用较低的强度以提高测试速度
     * @return 密码编码器
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        // 使用较低的强度(4)以提高测试速度，生产环境应使用默认强度(10)
        return new BCryptPasswordEncoder(4);
    }
}