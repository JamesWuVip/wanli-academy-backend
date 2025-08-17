package com.wanli.academy.backend.base;

import com.wanli.academy.backend.dto.LoginRequest;
import com.wanli.academy.backend.dto.RegisterRequest;
import com.wanli.academy.backend.dto.HomeworkCreateRequest;
import com.wanli.academy.backend.dto.HomeworkResponse;
import com.wanli.academy.backend.dto.QuestionCreateRequest;
import com.wanli.academy.backend.dto.QuestionResponse;

import java.util.Map;
import java.util.HashMap;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.Homework;
import com.wanli.academy.backend.entity.Question;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;

/**
 * 测试数据构建工具类
 * 提供标准化的测试数据创建方法，遵循DRY原则
 */
public class TestDataBuilder {

    // ==================== 用户相关测试数据 ====================

    /**
     * 创建测试用户实体
     */
    public static User buildTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Note: roles are handled separately in tests
        
        return user;
    }
    
    /**
     * 创建指定ID的测试用户实体
     */
    public static User buildTestUserWithId(Long id, String username, String email, String roleName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hashedpassword");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Note: roles are handled separately in tests
        
        return user;
    }

    public static User buildTestAdmin() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setEmail("admin@wanli.com");
        admin.setPassword("$2a$10$encoded.admin.password.hash");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setCreatedAt(LocalDateTime.now().minusDays(2));
        admin.setUpdatedAt(LocalDateTime.now());
        return admin;
    }

    public static User buildTestHqTeacher() {
        User teacher = new User();
        teacher.setId(3L);
        teacher.setUsername("hqteacher");
        teacher.setEmail("hqteacher@wanli.com");
        teacher.setPassword("$2a$10$encoded.teacher.password.hash");
        teacher.setFirstName("HQ");
        teacher.setLastName("Teacher");
        teacher.setCreatedAt(LocalDateTime.now().minusDays(3));
        teacher.setUpdatedAt(LocalDateTime.now());
        return teacher;
    }

    public static User buildTestStudent() {
        User student = new User();
        student.setId(4L);
        student.setUsername("student");
        student.setEmail("student@wanli.com");
        student.setPassword("$2a$10$encoded.student.password.hash");
        student.setFirstName("Test");
        student.setLastName("Student");
        student.setCreatedAt(LocalDateTime.now().minusDays(4));
        student.setUpdatedAt(LocalDateTime.now());
        return student;
    }

    // ==================== 角色相关测试数据 ====================

    public static Role buildStudentRole() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_STUDENT");
        role.setDescription("学生角色");
        return role;
    }

    public static Role buildAdminRole() {
        Role role = new Role();
        role.setId(2L);
        role.setName("ROLE_ADMIN");
        role.setDescription("管理员角色");
        return role;
    }

    public static Role buildHqTeacherRole() {
        Role role = new Role();
        role.setId(3L);
        role.setName("ROLE_HQ_TEACHER");
        role.setDescription("总部教师角色");
        return role;
    }

    public static Role buildFranchiseTeacherRole() {
        Role role = new Role();
        role.setId(4L);
        role.setName("ROLE_FRANCHISE_TEACHER");
        role.setDescription("加盟商教师角色");
        return role;
    }

    // ==================== 认证相关测试数据 ====================

    public static LoginRequest buildValidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("password123");
        return request;
    }

    public static LoginRequest buildInvalidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("invaliduser");
        request.setPassword("wrongpassword");
        return request;
    }

    public static RegisterRequest buildValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@wanli.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setPhoneNumber("+1234567890");
        return request;
    }

    public static RegisterRequest buildInvalidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(""); // 无效的用户名
        request.setEmail("invalid-email"); // 无效的邮箱格式
        request.setPassword("123"); // 密码太短
        request.setFirstName(""); // 空的姓
        request.setLastName(""); // 空的名
        return request;
    }

    // ==================== 作业相关测试数据 ====================

    public static Homework buildTestHomework() {
        Homework homework = new Homework();
        homework.setId(UUID.randomUUID());
        homework.setTitle("测试作业");
        homework.setDescription("这是一个测试作业的描述");
        homework.setCreatorId(1L); // HQ Teacher创建
        homework.setCreatedAt(LocalDateTime.now().minusHours(1));
        homework.setUpdatedAt(LocalDateTime.now());
        return homework;
    }
    
    /**
     * 创建测试作业响应DTO
     */
    public static HomeworkResponse buildTestHomeworkResponse() {
        HomeworkResponse response = new HomeworkResponse();
        response.setId(UUID.randomUUID());
        response.setTitle("测试作业");
        response.setDescription("这是一个测试作业的描述");
        response.setCreatedBy(1L);
        response.setCreatedByUsername("testuser");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        
        return response;
    }

    public static HomeworkCreateRequest buildValidHomeworkRequest() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        request.setTitle("新作业");
        request.setDescription("新作业的描述");
        return request;
    }

    public static HomeworkCreateRequest buildInvalidHomeworkRequest() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        request.setTitle(""); // 空标题
        request.setDescription(""); // 空描述
        return request;
    }

    // ==================== 题目相关测试数据 ====================

    /**
     * 创建测试题目实体
     */
    public static Question buildTestQuestion() {
        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setHomeworkId(UUID.randomUUID());
        question.setQuestionType("SINGLE_CHOICE");
        
        // 创建内容Map
        Map<String, Object> content = Map.of("text", "这是一道测试题目");
        question.setContent(content);
        
        // 创建标准答案Map
        Map<String, Object> standardAnswer = Map.of("answer", "这是标准答案");
        question.setStandardAnswer(standardAnswer);
        
        question.setOrderIndex(1);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        
        return question;
    }
    
    /**
     * 创建测试题目响应DTO
     */
    public static QuestionResponse buildTestQuestionResponse() {
        QuestionResponse response = new QuestionResponse();
        response.setId(UUID.randomUUID());
        response.setHomeworkId(UUID.randomUUID());
        response.setQuestionType("SINGLE_CHOICE");
        
        response.setContent("这是一个测试题目");
        
        response.setStandardAnswer("A");
        
        response.setOrderIndex(1);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        
        return response;
    }

    public static QuestionCreateRequest buildValidQuestionRequest() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setContent("新题目的问题？");
        request.setQuestionType("选择题");
        request.setStandardAnswer("选项1");
        request.setOrderIndex(1);
        return request;
    }

    public static QuestionCreateRequest buildInvalidQuestionCreateRequest() {
        QuestionCreateRequest request = new QuestionCreateRequest();
        request.setContent(""); // 空题目
        request.setQuestionType(""); // 空类型
        request.setStandardAnswer(""); // 空答案
        request.setOrderIndex(-1); // 无效顺序
        return request;
    }

    // ==================== 无效数据构建方法 ====================

    public static LoginRequest buildEmptyLoginRequest() {
        return new LoginRequest();
    }

    public static RegisterRequest buildEmptyRegisterRequest() {
        return new RegisterRequest();
    }

    public static HomeworkCreateRequest buildEmptyHomeworkRequest() {
        return new HomeworkCreateRequest();
    }

    public static QuestionCreateRequest buildEmptyQuestionRequest() {
        return new QuestionCreateRequest();
    }

    // ==================== JWT Token常量 ====================

    public static final String VALID_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiaWF0IjoxNTE2MjM5MDIyfQ.test";
    public static final String ADMIN_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6IkFETUlOIiwiaWF0IjoxNTE2MjM5MDIyfQ.admin";
    public static final String HQ_TEACHER_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzIiwicm9sZSI6IkhRX1RFQUNIRVIiLCJpYXQiOjE1MTYyMzkwMjJ9.hqteacher";
    public static final String STUDENT_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIyIiwicm9sZSI6IlNUVURFTlQiLCJpYXQiOjE1MTYyMzkwMjJ9.student";
    public static final String INVALID_JWT_TOKEN = "Bearer invalid.jwt.token";
    public static final String EXPIRED_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZXhwIjoxNTE2MjM5MDIyfQ.expired";
}