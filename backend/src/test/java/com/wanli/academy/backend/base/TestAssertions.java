package com.wanli.academy.backend.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试断言工具类
 * 提供标准化的验证方法，遵循DRY原则
 */
public class TestAssertions {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ==================== HTTP状态码断言 ====================

    public static void assertOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    public static void assertCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    public static void assertBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    public static void assertUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    public static void assertForbidden(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden());
    }

    public static void assertNotFound(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound());
    }

    public static void assertInternalServerError(ResultActions result) throws Exception {
        result.andExpect(status().isInternalServerError());
    }

    // ==================== JSON响应断言 ====================

    public static void assertJsonPath(ResultActions result, String path, Object expectedValue) throws Exception {
        result.andExpect(jsonPath(path, is(expectedValue)));
    }

    public static void assertJsonPathExists(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path).exists());
    }

    public static void assertJsonPathNotExists(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path).doesNotExist());
    }

    public static void assertJsonPathIsArray(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path, isA(Collection.class)));
    }

    public static void assertJsonPathArraySize(ResultActions result, String path, int expectedSize) throws Exception {
        result.andExpect(jsonPath(path, hasSize(expectedSize)));
    }

    public static void assertJsonPathNotEmpty(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path, not(emptyString())));
    }

    public static void assertJsonPathIsNull(ResultActions result, String path) throws Exception {
        result.andExpect(jsonPath(path).isEmpty());
    }

    // ==================== 错误响应断言 ====================

    public static void assertErrorResponse(ResultActions result, String expectedMessage) throws Exception {
        result.andExpect(jsonPath("$.message", is(expectedMessage)));
    }

    public static void assertErrorResponseContains(ResultActions result, String expectedMessagePart) throws Exception {
        result.andExpect(jsonPath("$.message", containsString(expectedMessagePart)));
    }

    public static void assertValidationError(ResultActions result, String field, String expectedMessage) throws Exception {
        result.andExpect(jsonPath("$.errors." + field, is(expectedMessage)));
    }

    public static void assertValidationErrors(ResultActions result, int expectedErrorCount) throws Exception {
        result.andExpect(jsonPath("$.errors", aMapWithSize(expectedErrorCount)));
    }

    // ==================== 认证相关断言 ====================

    public static void assertAuthenticationRequired(ResultActions result) throws Exception {
        assertUnauthorized(result);
        assertErrorResponseContains(result, "认证");
    }

    public static void assertInsufficientPermissions(ResultActions result) throws Exception {
        assertForbidden(result);
        assertErrorResponseContains(result, "权限");
    }

    public static void assertJwtTokenPresent(ResultActions result) throws Exception {
        result.andExpect(jsonPath("$.token").exists())
              .andExpect(jsonPath("$.token", not(emptyString())));
    }

    public static void assertJwtTokenValid(ResultActions result) throws Exception {
        assertJwtTokenPresent(result);
        result.andExpect(jsonPath("$.token", startsWith("eyJ")));
    }

    // ==================== 用户相关断言 ====================

    public static void assertUserResponse(ResultActions result, String expectedUsername, String expectedEmail) throws Exception {
        result.andExpect(jsonPath("$.username", is(expectedUsername)))
              .andExpect(jsonPath("$.email", is(expectedEmail)))
              .andExpect(jsonPath("$.password").doesNotExist()); // 密码不应该返回
    }

    public static void assertUserListResponse(ResultActions result, int expectedSize) throws Exception {
        assertJsonPathIsArray(result, "$");
        assertJsonPathArraySize(result, "$", expectedSize);
        result.andExpect(jsonPath("$[0].username").exists())
              .andExpect(jsonPath("$[0].email").exists())
              .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    // ==================== 作业相关断言 ====================

    public static void assertHomeworkResponse(ResultActions result, String expectedTitle, String expectedDescription) throws Exception {
        result.andExpect(jsonPath("$.title", is(expectedTitle)))
              .andExpect(jsonPath("$.description", is(expectedDescription)))
              .andExpect(jsonPath("$.id").exists())
              .andExpect(jsonPath("$.createdBy").exists())
              .andExpect(jsonPath("$.createdAt").exists());
    }

    public static void assertHomeworkListResponse(ResultActions result, int expectedSize) throws Exception {
        assertJsonPathIsArray(result, "$");
        assertJsonPathArraySize(result, "$", expectedSize);
        if (expectedSize > 0) {
            result.andExpect(jsonPath("$[0].id").exists())
                  .andExpect(jsonPath("$[0].title").exists())
                  .andExpect(jsonPath("$[0].description").exists());
        }
    }

    // ==================== 题目相关断言 ====================

    public static void assertQuestionResponse(ResultActions result, Object expectedContent, String expectedType) throws Exception {
        result.andExpect(jsonPath("$.content", is(expectedContent)))
              .andExpect(jsonPath("$.questionType", is(expectedType)))
              .andExpect(jsonPath("$.id").exists())
              .andExpect(jsonPath("$.homeworkId").exists())
              .andExpect(jsonPath("$.orderIndex").exists());
    }

    public static void assertQuestionListResponse(ResultActions result, int expectedSize) throws Exception {
        assertJsonPathIsArray(result, "$");
        assertJsonPathArraySize(result, "$", expectedSize);
        if (expectedSize > 0) {
            result.andExpect(jsonPath("$[0].id").exists())
                  .andExpect(jsonPath("$[0].questionText").exists())
                  .andExpect(jsonPath("$[0].questionType").exists());
        }
    }

    // ==================== 实体对象断言 ====================

    public static void assertNotNull(Object object, String message) {
        assertNotNull(object, message);
    }

    public static void assertNull(Object object, String message) {
        assertNull(object, message);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        assertEquals(expected, actual, message);
    }

    public static void assertTrue(boolean condition, String message) {
        assertTrue(condition, message);
    }

    public static void assertFalse(boolean condition, String message) {
        assertFalse(condition, message);
    }

    // ==================== 集合断言 ====================

    public static void assertCollectionNotEmpty(Collection<?> collection, String message) {
        assertNotNull(collection, message + " - 集合不应为null");
        assertFalse(collection.isEmpty(), message + " - 集合不应为空");
    }

    public static void assertCollectionEmpty(Collection<?> collection, String message) {
        if (collection != null) {
            assertTrue(collection.isEmpty(), message + " - 集合应为空");
        }
    }

    public static void assertCollectionSize(Collection<?> collection, int expectedSize, String message) {
        assertNotNull(collection, message + " - 集合不应为null");
        assertEquals(expectedSize, collection.size(), message + " - 集合大小不匹配");
    }

    // ==================== UUID断言 ====================

    public static void assertValidUuid(String uuidString, String message) {
        assertNotNull(uuidString, message + " - UUID字符串不应为null");
        assertDoesNotThrow(() -> UUID.fromString(uuidString), message + " - 应为有效的UUID格式");
    }

    public static void assertValidUuid(UUID uuid, String message) {
        assertNotNull(uuid, message + " - UUID不应为null");
    }

    // ==================== 时间断言 ====================

    public static void assertTimeNotNull(LocalDateTime dateTime, String message) {
        assertNotNull(dateTime, message + " - 时间不应为null");
    }

    public static void assertTimeBefore(LocalDateTime earlier, LocalDateTime later, String message) {
        assertNotNull(earlier, message + " - 较早时间不应为null");
        assertNotNull(later, message + " - 较晚时间不应为null");
        assertTrue(earlier.isBefore(later), message + " - 时间顺序不正确");
    }

    public static void assertTimeAfter(LocalDateTime later, LocalDateTime earlier, String message) {
        assertNotNull(earlier, message + " - 较早时间不应为null");
        assertNotNull(later, message + " - 较晚时间不应为null");
        assertTrue(later.isAfter(earlier), message + " - 时间顺序不正确");
    }

    public static void assertTimeWithinRange(LocalDateTime actual, LocalDateTime start, LocalDateTime end, String message) {
        assertNotNull(actual, message + " - 实际时间不应为null");
        assertNotNull(start, message + " - 开始时间不应为null");
        assertNotNull(end, message + " - 结束时间不应为null");
        assertTrue(actual.isAfter(start) || actual.isEqual(start), message + " - 时间应在范围内");
        assertTrue(actual.isBefore(end) || actual.isEqual(end), message + " - 时间应在范围内");
    }

    // ==================== JSON解析工具方法 ====================

    public static JsonNode parseJson(String jsonString) throws Exception {
        return objectMapper.readTree(jsonString);
    }

    public static <T> T parseJsonAs(String jsonString, Class<T> clazz) throws Exception {
        return objectMapper.readValue(jsonString, clazz);
    }
}