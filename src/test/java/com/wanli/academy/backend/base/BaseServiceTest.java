package com.wanli.academy.backend.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service层测试基类
 * 提供统一的测试配置和通用工具方法，遵循DRY原则
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseServiceTest {

    protected ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 测试常量 ====================
    
    protected static final String TEST_USERNAME = "testuser";
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_PASSWORD = "password123";
    protected static final String TEST_HOMEWORK_TITLE = "测试作业";
    protected static final String TEST_HOMEWORK_DESCRIPTION = "测试作业描述";
    protected static final String TEST_QUESTION_CONTENT = "测试题目内容";
    protected static final String TEST_QUESTION_TYPE = "SINGLE_CHOICE";
    
    protected static final UUID TEST_HOMEWORK_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    protected static final UUID TEST_QUESTION_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    protected static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
    
    protected static final Long TEST_ROLE_ID = 1L;
    protected static final String TEST_ROLE_NAME = "ROLE_HQ_TEACHER";
    
    @BeforeEach
    void baseSetUp() {
        // 子类可以重写此方法进行特定的初始化
    }
    
    // ==================== 通用工具方法 ====================
    
    /**
     * 获取当前时间
     */
    protected LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }
    
    /**
     * 生成随机UUID
     */
    protected UUID generateRandomUUID() {
        return UUID.randomUUID();
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
    
    /**
     * 将JSON字符串转换为对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
    
    /**
     * 验证UUID格式
     */
    protected boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 验证时间是否在指定范围内（用于测试创建时间等）
     */
    protected boolean isTimeWithinRange(LocalDateTime time, LocalDateTime start, LocalDateTime end) {
        return time.isAfter(start) && time.isBefore(end);
    }
}