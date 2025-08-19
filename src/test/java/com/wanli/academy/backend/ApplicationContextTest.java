package com.wanli.academy.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用上下文加载测试
 * 验证Spring Boot应用能否正常启动
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    /**
     * 测试应用上下文是否能够成功加载
     */
    @Test
    void shouldLoadApplicationContext() {
        // 如果应用上下文加载成功，此测试就会通过
        // 这是最基本的Spring Boot集成测试
    }
}