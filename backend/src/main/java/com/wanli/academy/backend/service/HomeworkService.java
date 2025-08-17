package com.wanli.academy.backend.service;

import com.wanli.academy.backend.dto.HomeworkCreateRequest;
import com.wanli.academy.backend.dto.HomeworkResponse;
import com.wanli.academy.backend.dto.QuestionCreateRequest;
import com.wanli.academy.backend.dto.QuestionResponse;
import com.wanli.academy.backend.entity.Homework;
import com.wanli.academy.backend.entity.Question;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.repository.HomeworkRepository;
import com.wanli.academy.backend.repository.QuestionRepository;
import com.wanli.academy.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作业服务类
 * 处理作业相关的业务逻辑
 */
@Service
@Transactional
public class HomeworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeworkService.class);
    
    @Autowired
    private HomeworkRepository homeworkRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 创建新作业
     * @param request 创建作业请求
     * @return 创建的作业响应
     */
    public HomeworkResponse createHomework(HomeworkCreateRequest request) {
        logger.info("Creating new homework with title: {}", request.getTitle());
        
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 创建作业实体
        Homework homework = new Homework();
        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setCreatorId(currentUser.getId());
        homework.setCreator(currentUser);  // 设置完整的User对象
        homework.setCreatedAt(LocalDateTime.now());
        homework.setUpdatedAt(LocalDateTime.now());
        
        // 保存作业
        Homework savedHomework = homeworkRepository.save(homework);
        
        logger.info("Successfully created homework with ID: {}", savedHomework.getId());
        
        return convertToHomeworkResponse(savedHomework);
    }
    
    /**
     * 获取当前用户创建的作业列表
     * @return 作业列表
     */
    public List<HomeworkResponse> getHomeworksByCreator() {
        logger.info("Fetching homeworks for current user");
        
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 查询当前用户创建的作业
        List<Homework> homeworks = homeworkRepository.findByCreatorIdOrderByCreatedAtDesc(currentUser.getId());
        
        logger.info("Found {} homeworks for user: {}", homeworks.size(), currentUser.getUsername());
        
        return homeworks.stream()
                .map(this::convertToHomeworkResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 向作业添加题目
     * @param homeworkId 作业ID
     * @param request 题目创建请求
     * @return 创建的题目响应
     */
    public QuestionResponse addQuestionToHomework(UUID homeworkId, QuestionCreateRequest request) {
        logger.info("Adding question to homework: {}", homeworkId);
        
        // 验证作业存在且属于当前用户
        Homework homework = validateHomeworkOwnership(homeworkId);
        
        // 创建题目实体
        Question question = new Question();
        // 将String转换为Map<String, Object>以适配JSONB字段
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", request.getContent());
        question.setContent(contentMap);
        
        question.setQuestionType(request.getQuestionType());
        
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("text", request.getStandardAnswer());
        question.setStandardAnswer(answerMap);
        question.setOrderIndex(request.getOrderIndex());
        question.setHomework(homework);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        
        // 保存题目
        Question savedQuestion = questionRepository.save(question);
        
        logger.info("Successfully added question with ID: {} to homework: {}", 
                   savedQuestion.getId(), homeworkId);
        
        return convertToQuestionResponse(savedQuestion);
    }
    
    /**
     * 从JSONB字段中提取文本内容
     */
    private String extractTextFromJsonb(Map<String, Object> jsonbField) {
        if (jsonbField == null) {
            return null;
        }
        Object text = jsonbField.get("text");
        return text != null ? text.toString() : null;
    }
    
    /**
     * 验证作业所有权
     * @param homeworkId 作业ID
     * @return 作业实体
     * @throws RuntimeException 如果作业不存在
     * @throws AccessDeniedException 如果用户无权访问该作业
     */
    public Homework validateHomeworkOwnership(UUID homeworkId) {
        logger.debug("Validating homework ownership for ID: {}", homeworkId);
        
        // 查找作业
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> {
                    logger.warn("Homework not found with ID: {}", homeworkId);
                    return new RuntimeException("作业不存在");
                });
        
        // 获取当前用户
        User currentUser = getCurrentUser();
        
        // 验证所有权
        if (!homework.getCreator().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to access homework {} owned by user {}", 
                       currentUser.getUsername(), homeworkId, homework.getCreator().getUsername());
            throw new AccessDeniedException("您无权访问此作业");
        }
        
        return homework;
    }
    
    /**
     * 获取当前登录用户
     * @return 当前用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Current user not found: {}", username);
                    return new RuntimeException("当前用户不存在");
                });
    }
    
    /**
     * 转换作业实体为响应DTO
     * @param homework 作业实体
     * @return 作业响应DTO
     */
    private HomeworkResponse convertToHomeworkResponse(Homework homework) {
        HomeworkResponse response = new HomeworkResponse();
        response.setId(homework.getId());
        response.setTitle(homework.getTitle());
        response.setDescription(homework.getDescription());
        response.setCreatedBy(homework.getCreator().getId());
        response.setCreatedByUsername(homework.getCreator().getUsername());
        response.setCreatedAt(homework.getCreatedAt());
        response.setUpdatedAt(homework.getUpdatedAt());
        
        // 如果需要包含题目列表，可以在这里添加
        if (homework.getQuestions() != null) {
            List<QuestionResponse> questionResponses = homework.getQuestions().stream()
                    .map(this::convertToQuestionResponse)
                    .collect(Collectors.toList());
            response.setQuestions(questionResponses);
        }
        
        return response;
    }
    
    /**
     * 转换题目实体为响应DTO
     * @param question 题目实体
     * @return 题目响应DTO
     */
    private QuestionResponse convertToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setContent(extractTextFromJsonb(question.getContent()));
        response.setQuestionType(question.getQuestionType());
        response.setStandardAnswer(extractTextFromJsonb(question.getStandardAnswer()));
        response.setOrderIndex(question.getOrderIndex());
        response.setHomeworkId(question.getHomework().getId());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        
        return response;
    }
}