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
 * Homework service class
 * Handles homework-related business logic
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
     * Create new homework
     * @param request homework creation request
     * @return created homework response
     */
    public HomeworkResponse createHomework(HomeworkCreateRequest request) {
        logger.info("Creating new homework with title: {}", request.getTitle());
        
        // Get current logged-in user
        User currentUser = getCurrentUser();
        
        // Create homework entity
        Homework homework = new Homework();
        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setCreatorId(currentUser.getId());
        homework.setCreator(currentUser);  // Set complete User object
        homework.setCreatedAt(LocalDateTime.now());
        homework.setUpdatedAt(LocalDateTime.now());
        
        // Save homework
        Homework savedHomework = homeworkRepository.save(homework);
        
        logger.info("Successfully created homework with ID: {}", savedHomework.getId());
        
        return convertToHomeworkResponse(savedHomework);
    }
    
    /**
     * Get homework list created by current user
     * @return homework list
     */
    public List<HomeworkResponse> getHomeworksByCreator() {
        logger.info("Fetching homeworks for current user");
        
        // Get current logged-in user
        User currentUser = getCurrentUser();
        
        // Query homework created by current user
        List<Homework> homeworks = homeworkRepository.findByCreatorIdOrderByCreatedAtDesc(currentUser.getId());
        
        logger.info("Found {} homeworks for user: {}", homeworks.size(), currentUser.getUsername());
        
        return homeworks.stream()
                .map(this::convertToHomeworkResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Add question to homework
     * @param homeworkId homework ID
     * @param request question creation request
     * @return created question response
     */
    @Transactional
    public QuestionResponse addQuestionToHomework(UUID homeworkId, QuestionCreateRequest request) {
        logger.info("Adding question to homework: {}", homeworkId);
        
        // Validate homework exists and belongs to current user
        Homework homework = validateHomeworkOwnership(homeworkId);
        
        // Create question entity
        Question question = new Question();
        // Convert String to Map<String, Object> to adapt JSONB field
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", request.getContent());
        question.setContent(contentMap);
        
        question.setQuestionType(request.getQuestionType());
        
        Map<String, Object> answerMap = new HashMap<>();
        answerMap.put("text", request.getStandardAnswer());
        question.setStandardAnswer(answerMap);
        question.setOrderIndex(request.getOrderIndex());
        // Only set homeworkId, not homework object, to avoid JPA association conflicts
        question.setHomeworkId(homeworkId);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        
        // Save question
        Question savedQuestion = questionRepository.save(question);
        
        logger.info("Successfully added question with ID: {} to homework: {}", 
                   savedQuestion.getId(), homeworkId);
        
        return convertToQuestionResponse(savedQuestion);
    }
    
    /**
     * Extract text content from JSONB field
     */
    private String extractTextFromJsonb(Map<String, Object> jsonbField) {
        if (jsonbField == null) {
            return null;
        }
        Object text = jsonbField.get("text");
        return text != null ? text.toString() : null;
    }
    
    /**
     * Validate homework ownership
     * @param homeworkId homework ID
     * @return homework entity
     * @throws RuntimeException if homework does not exist
     * @throws AccessDeniedException if user has no access to the homework
     */
    public Homework validateHomeworkOwnership(UUID homeworkId) {
        logger.debug("Validating homework ownership for ID: {}", homeworkId);
        
        // Find homework
        Homework homework = homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> {
                    logger.warn("Homework not found with ID: {}", homeworkId);
                    return new RuntimeException("Homework does not exist");
                });
        
        // Get current user
        User currentUser = getCurrentUser();
        
        // Validate ownership
        if (!homework.getCreator().getId().equals(currentUser.getId())) {
            logger.warn("User {} attempted to access homework {} owned by user {}", 
                       currentUser.getUsername(), homeworkId, homework.getCreator().getUsername());
            throw new AccessDeniedException("You have no access to this homework");
        }
        
        return homework;
    }
    
    /**
     * Get current logged-in user
     * @return current user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Current user not found: {}", username);
                    return new RuntimeException("Current user does not exist");
                });
    }
    
    /**
     * Convert homework entity to response DTO
     * @param homework homework entity
     * @return homework response DTO
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
        
        // If need to include question list, can add here
        if (homework.getQuestions() != null) {
            List<QuestionResponse> questionResponses = homework.getQuestions().stream()
                    .map(this::convertToQuestionResponse)
                    .collect(Collectors.toList());
            response.setQuestions(questionResponses);
        }
        
        return response;
    }
    
    /**
     * Convert question entity to response DTO
     * @param question question entity
     * @return question response DTO
     */
    private QuestionResponse convertToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setContent(extractTextFromJsonb(question.getContent()));
        response.setQuestionType(question.getQuestionType());
        response.setStandardAnswer(extractTextFromJsonb(question.getStandardAnswer()));
        response.setOrderIndex(question.getOrderIndex());
        // Use homework association to get homework ID, if homework is null then use homeworkId field as fallback
        response.setHomeworkId(question.getHomework() != null ? question.getHomework().getId() : question.getHomeworkId());
        response.setCreatedAt(question.getCreatedAt());
        response.setUpdatedAt(question.getUpdatedAt());
        // Add new fields
        response.setExplanation(question.getExplanation());
        response.setVideoUrl(question.getVideoUrl());
        
        return response;
    }
}