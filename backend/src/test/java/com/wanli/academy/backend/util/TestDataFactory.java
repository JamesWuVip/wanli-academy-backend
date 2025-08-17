package com.wanli.academy.backend.util;

import com.wanli.academy.backend.dto.HomeworkCreateRequest;
import com.wanli.academy.backend.dto.QuestionCreateRequest;
import com.wanli.academy.backend.entity.Homework;
import com.wanli.academy.backend.entity.Question;
import com.wanli.academy.backend.entity.Role;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 测试数据工厂类
 * 提供统一的测试数据创建方法，遵循DRY原则
 */
public final class TestDataFactory {
    
    // 测试常量
    public static final Long TEST_USER_ID = 1L;
    public static final String DEFAULT_HOMEWORK_TITLE = "测试作业标题";
    public static final String DEFAULT_HOMEWORK_DESCRIPTION = "这是一个测试作业的描述";
    public static final String DEFAULT_QUESTION_CONTENT = "这是一道测试题目";
    public static final String DEFAULT_STANDARD_ANSWER = "这是标准答案";
    
    // 私有构造函数，防止实例化
    private TestDataFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 构建测试用的作业创建请求
     */
    public static HomeworkCreateRequest buildHomeworkCreateRequest() {
        return new HomeworkCreateRequest("测试作业", "这是一个测试作业");
    }

    /**
     * 构建自定义标题的作业创建请求
     * 
     * @param title 作业标题
     * @return HomeworkCreateRequest
     */
    public static HomeworkCreateRequest buildHomeworkRequestWithTitle(String title) {
        return new HomeworkCreateRequest(title, "这是一个测试作业");
    }

    /**
     * 构建测试用的题目创建请求
     */
    public static QuestionCreateRequest buildQuestionCreateRequest() {
        return new QuestionCreateRequest(DEFAULT_QUESTION_CONTENT, "选择题", DEFAULT_STANDARD_ANSWER, 1);
    }

    /**
     * 构建默认的作业创建请求
     * 
     * @return HomeworkCreateRequest
     */
    public static HomeworkCreateRequest buildDefaultHomeworkRequest() {
        HomeworkCreateRequest request = new HomeworkCreateRequest();
        request.setTitle(DEFAULT_HOMEWORK_TITLE);
        request.setDescription(DEFAULT_HOMEWORK_DESCRIPTION);
        return request;
    }
    
    /**
     * 构建默认的题目创建请求
     * 
     * @return QuestionCreateRequest
     */
    public static QuestionCreateRequest buildDefaultQuestionRequest() {
        return buildQuestionCreateRequest();
    }

    /**
     * 构建自定义内容的题目创建请求
     * 
     * @param content 题目内容
     * @return QuestionCreateRequest
     */
    public static QuestionCreateRequest buildQuestionRequestWithContent(String content) {
        return new QuestionCreateRequest(content, "选择题", "A", 1);
    }

    /**
     * 构建测试用的作业实体
     */
    public static Homework buildHomework() {
        return new Homework("测试作业", "这是一个测试作业", TEST_USER_ID);
    }
     
    /**
     * 构建自定义创建者的作业实体
     * 
     * @param creatorId 创建者ID
     * @return Homework
     */
    public static Homework buildHomeworkWithCreator(Long creatorId) {
        return new Homework("测试作业", "这是一个测试作业", creatorId);
    }

    /**
     * 构建默认的题目实体
     * 
     * @return Question
     */
    public static Question buildDefaultQuestion() {
        Question question = new Question();
        
        // 创建content Map
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", DEFAULT_QUESTION_CONTENT);
        question.setContent(contentMap);
        
        question.setQuestionType("选择题");
        
        // 创建standardAnswer Map
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("answer", DEFAULT_STANDARD_ANSWER);
        question.setStandardAnswer(answerMap);
        
        question.setOrderIndex(1);
        return question;
    }

    /**
     * 构建角色实体
     * 
     * @param roleName 角色名称
     * @return Role
     */
    public static Role buildRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return role;
    }

    /**
     * 构建HQ教师角色
     * 
     * @return Role
     */
    public static Role buildHqTeacherRole() {
        return buildRole("ROLE_HQ_TEACHER");
    }

    /**
     * 构建学生角色
     * 
     * @return Role
     */
    public static Role buildStudentRole() {
        return buildRole("ROLE_STUDENT");
    }
}