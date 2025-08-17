package com.wanli.academy.backend.service;

import com.wanli.academy.backend.base.BaseServiceTest;
import com.wanli.academy.backend.dto.HomeworkCreateRequest;
import com.wanli.academy.backend.dto.HomeworkResponse;
import com.wanli.academy.backend.dto.QuestionCreateRequest;
import com.wanli.academy.backend.dto.QuestionResponse;
import com.wanli.academy.backend.entity.Homework;
import com.wanli.academy.backend.entity.Question;
import com.wanli.academy.backend.entity.User;
import com.wanli.academy.backend.entity.Role;
// ResourceNotFoundException removed as it doesn't exist in the project
import org.springframework.security.access.AccessDeniedException;
import com.wanli.academy.backend.repository.HomeworkRepository;
import com.wanli.academy.backend.repository.QuestionRepository;
import com.wanli.academy.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * HomeworkService测试类
 * 遵循should_whenCondition_thenExpectedResult命名格式
 */
class HomeworkServiceTest extends BaseServiceTest {

    @Mock
    private HomeworkRepository homeworkRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeworkService homeworkService;

    private User testUser;
    private Role testRole;
    private Homework testHomework;
    private Question testQuestion;
    private HomeworkCreateRequest validRequest;
    private QuestionCreateRequest validQuestionRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        setupTestData();
        setupSecurityContext();
    }

    private void setupTestData() {
        // 创建测试角色
        testRole = new Role();
        testRole.setId(TEST_ROLE_ID);
        testRole.setName(TEST_ROLE_NAME);

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(TEST_USERNAME);
        testUser.setEmail(TEST_EMAIL);
        testUser.addRole(testRole);
        testUser.setCreatedAt(getCurrentTime());
        testUser.setUpdatedAt(getCurrentTime());

        // 创建测试作业
        testHomework = new Homework();
        testHomework.setId(TEST_HOMEWORK_ID);
        testHomework.setTitle(TEST_HOMEWORK_TITLE);
        testHomework.setDescription(TEST_HOMEWORK_DESCRIPTION);
        testHomework.setCreator(testUser);
        testHomework.setCreatedAt(getCurrentTime());
        testHomework.setUpdatedAt(getCurrentTime());

        // 创建测试题目
        testQuestion = new Question();
        testQuestion.setId(TEST_QUESTION_ID);
        testQuestion.setHomeworkId(TEST_HOMEWORK_ID);
        testQuestion.setQuestionType(TEST_QUESTION_TYPE);
        Map<String, Object> content = new HashMap<>();
        content.put("text", TEST_QUESTION_CONTENT);
        testQuestion.setContent(content);
        testQuestion.setCreatedAt(getCurrentTime());
        testQuestion.setUpdatedAt(getCurrentTime());

        // 创建有效的请求对象
        validRequest = new HomeworkCreateRequest();
        validRequest.setTitle(TEST_HOMEWORK_TITLE);
        validRequest.setDescription(TEST_HOMEWORK_DESCRIPTION);

        validQuestionRequest = new QuestionCreateRequest();
        validQuestionRequest.setQuestionType(TEST_QUESTION_TYPE);
        validQuestionRequest.setContent("这是一道数学题：计算2+2等于多少？");
    }

    private void setupSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(TEST_USERNAME);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ==================== createHomework方法测试 ====================

    @Test
    void should_createHomework_when_validRequest() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        
        Homework savedHomework = new Homework();
        savedHomework.setId(UUID.randomUUID());
        savedHomework.setTitle(validRequest.getTitle());
        savedHomework.setDescription(validRequest.getDescription());
        savedHomework.setCreator(testUser);
        savedHomework.setCreatedAt(LocalDateTime.now());
        
        when(homeworkRepository.save(any(Homework.class))).thenReturn(savedHomework);

        // When
        HomeworkResponse result = homeworkService.createHomework(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(validRequest.getTitle(), result.getTitle());
        assertEquals(validRequest.getDescription(), result.getDescription());
        assertEquals(testUser.getUsername(), result.getCreatedByUsername());

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository).save(any(Homework.class));
    }

    @Test
    void should_throwRuntimeException_when_userNotFound() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.createHomework(validRequest);
        });

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository, never()).save(any(Homework.class));
    }

    @Test
    void should_throwNullPointerException_when_nullRequest() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            homeworkService.createHomework(null);
        });

        verify(userRepository, never()).findByUsername(anyString());
        verify(homeworkRepository, never()).save(any(Homework.class));
    }

    @Test
    void should_createHomework_when_emptyTitle() {
        // Given
        HomeworkCreateRequest emptyTitleRequest = new HomeworkCreateRequest();
        emptyTitleRequest.setTitle("");
        emptyTitleRequest.setDescription("测试描述");
        
        Homework savedHomework = new Homework();
        savedHomework.setId(UUID.randomUUID());
        savedHomework.setTitle("");
        savedHomework.setDescription("测试描述");
        savedHomework.setCreator(testUser);
        savedHomework.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(homeworkRepository.save(any(Homework.class))).thenReturn(savedHomework);
        
        // When
        HomeworkResponse result = homeworkService.createHomework(emptyTitleRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.getTitle());
        assertEquals("测试描述", result.getDescription());
        
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository).save(any(Homework.class));
    }

    @Test
    void should_createHomework_when_nullTitle() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        
        HomeworkCreateRequest requestWithNullTitle = new HomeworkCreateRequest();
        requestWithNullTitle.setTitle(null);
        requestWithNullTitle.setDescription("测试描述");
        
        Homework savedHomework = new Homework();
        savedHomework.setId(UUID.randomUUID());
        savedHomework.setTitle(null);
        savedHomework.setDescription("测试描述");
        savedHomework.setCreator(testUser);
        savedHomework.setCreatedAt(LocalDateTime.now());
        savedHomework.setUpdatedAt(LocalDateTime.now());
        
        when(homeworkRepository.save(any(Homework.class))).thenReturn(savedHomework);
        
        // When
        HomeworkResponse result = homeworkService.createHomework(requestWithNullTitle);
        
        // Then
        assertNotNull(result);
        assertNull(result.getTitle());
        assertEquals("测试描述", result.getDescription());
        assertEquals(testUser.getUsername(), result.getCreatedByUsername());
        
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository).save(any(Homework.class));
    }

    // ==================== getHomeworksByCreator方法测试 ====================

    @Test
    void should_returnHomeworkList_when_validUser() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        List<Homework> homeworks = Arrays.asList(testHomework);
        when(homeworkRepository.findByCreatorIdOrderByCreatedAtDesc(testUser.getId())).thenReturn(homeworks);

        // When
        List<HomeworkResponse> result = homeworkService.getHomeworksByCreator();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testHomework.getTitle(), result.get(0).getTitle());
        assertEquals(testHomework.getDescription(), result.get(0).getDescription());
        assertEquals(testUser.getUsername(), result.get(0).getCreatedByUsername());

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository).findByCreatorIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void should_returnEmptyList_when_noHomeworks() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(homeworkRepository.findByCreatorIdOrderByCreatedAtDesc(testUser.getId())).thenReturn(Collections.emptyList());

        // When
        List<HomeworkResponse> result = homeworkService.getHomeworksByCreator();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository).findByCreatorIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void should_throwRuntimeException_when_userNotFoundInGetHomeworks() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.getHomeworksByCreator();
        });

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository, never()).findByCreatorIdOrderByCreatedAtDesc(anyLong());
    }

    // ==================== addQuestionToHomework方法测试 ====================

    @Test
    void should_addQuestion_when_validRequest() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        
        QuestionCreateRequest questionRequest = new QuestionCreateRequest();
        questionRequest.setContent("这是一道数学题：计算2+2等于多少？");
        questionRequest.setQuestionType("MULTIPLE_CHOICE");
        questionRequest.setOrderIndex(1);
        
        Question savedQuestion = new Question();
        savedQuestion.setId(UUID.randomUUID());
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", questionRequest.getContent());
        savedQuestion.setContent(contentMap);
        savedQuestion.setQuestionType(questionRequest.getQuestionType());
        savedQuestion.setOrderIndex(questionRequest.getOrderIndex());
        savedQuestion.setHomework(testHomework);
        
        when(homeworkRepository.findById(testHomework.getId())).thenReturn(Optional.of(testHomework));
        when(questionRepository.save(any(Question.class))).thenReturn(savedQuestion);
        
        // When
        QuestionResponse result = homeworkService.addQuestionToHomework(testHomework.getId(), questionRequest);
        
        // Then
        assertNotNull(result);
        // HomeworkService中convertToQuestionResponse使用extractTextFromJsonb从content.text获取内容
        // 但addQuestionToHomework中设置的是content.text，所以应该能正确获取
        assertEquals("这是一道数学题：计算2+2等于多少？", result.getContent());
        assertEquals(questionRequest.getQuestionType(), result.getQuestionType());
        assertEquals(questionRequest.getOrderIndex(), result.getOrderIndex());
        
        verify(homeworkRepository).findById(testHomework.getId());
        verify(questionRepository).save(any(Question.class));
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    void should_throwRuntimeException_when_homeworkNotFound() {
        // Given
        when(homeworkRepository.findById(TEST_HOMEWORK_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.addQuestionToHomework(TEST_HOMEWORK_ID, validQuestionRequest);
        });

        verify(homeworkRepository).findById(TEST_HOMEWORK_ID);
        verify(questionRepository, never()).save(any(Question.class));
        // userRepository不会被调用，因为在validateHomeworkOwnership中作业不存在就直接抛异常了
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void should_throwUnauthorizedException_when_notHomeworkOwner() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");
        
        Homework anotherHomework = new Homework();
        anotherHomework.setId(TEST_HOMEWORK_ID);
        anotherHomework.setCreator(anotherUser);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(homeworkRepository.findById(TEST_HOMEWORK_ID)).thenReturn(Optional.of(anotherHomework));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            homeworkService.addQuestionToHomework(TEST_HOMEWORK_ID, validQuestionRequest);
        });

        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(homeworkRepository).findById(TEST_HOMEWORK_ID);
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void should_throwRuntimeException_when_nullHomeworkId() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.addQuestionToHomework(null, validQuestionRequest);
        });

        verify(userRepository, never()).findByUsername(anyString());
        verify(homeworkRepository, never()).findById(any(UUID.class));
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void should_throwRuntimeException_when_nullQuestionRequest() {
        // Given
        when(homeworkRepository.findById(TEST_HOMEWORK_ID)).thenReturn(Optional.of(testHomework));
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.addQuestionToHomework(TEST_HOMEWORK_ID, null);
        });

        // validateHomeworkOwnership会被调用，所以这些验证是必要的
        verify(homeworkRepository).findById(TEST_HOMEWORK_ID);
        verify(userRepository).findByUsername(TEST_USERNAME);
        verify(questionRepository, never()).save(any(Question.class));
    }

    // ==================== validateHomeworkOwnership方法测试 ====================

    @Test
    void should_validateOwnership_when_userIsOwner() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(homeworkRepository.findById(testHomework.getId())).thenReturn(Optional.of(testHomework));
        
        // When
        Homework result = homeworkService.validateHomeworkOwnership(testHomework.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(testHomework.getId(), result.getId());
        assertEquals(testHomework.getCreator().getId(), result.getCreator().getId());
        
        verify(homeworkRepository).findById(testHomework.getId());
    }

    @Test
    void should_throwRuntimeException_when_userIsNotOwner() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setUsername("anotheruser");
        
        Homework anotherHomework = new Homework();
        anotherHomework.setId(UUID.randomUUID());
        anotherHomework.setCreator(anotherUser);
        
        when(homeworkRepository.findById(anotherHomework.getId())).thenReturn(Optional.of(anotherHomework));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.validateHomeworkOwnership(anotherHomework.getId());
        });
        
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    // ==================== 边界条件和异常处理测试 ====================

    @Test
    void should_handleNullAuthentication_when_securityContextIsNull() {
        // Given
        SecurityContextHolder.clearContext();
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            homeworkService.createHomework(validRequest);
        });
    }

    @Test
    void should_handleComplexQuestionContent_when_addingQuestion() {
        // Given
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        
        QuestionCreateRequest questionRequest = new QuestionCreateRequest();
        questionRequest.setContent("复杂题目内容，包含特殊字符：@#$%^&*()_+{}|:<>?[]\\");
        questionRequest.setQuestionType("ESSAY");
        questionRequest.setOrderIndex(2);
        
        Question savedQuestion = new Question();
        savedQuestion.setId(UUID.randomUUID());
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", questionRequest.getContent());
        savedQuestion.setContent(contentMap);
        savedQuestion.setQuestionType(questionRequest.getQuestionType());
        savedQuestion.setOrderIndex(questionRequest.getOrderIndex());
        savedQuestion.setHomework(testHomework);
        
        when(homeworkRepository.findById(testHomework.getId())).thenReturn(Optional.of(testHomework));
        when(questionRepository.save(any(Question.class))).thenReturn(savedQuestion);
        
        // When
        QuestionResponse result = homeworkService.addQuestionToHomework(testHomework.getId(), questionRequest);
        
        // Then
        assertNotNull(result);
        // 验证复杂内容能正确处理
        assertEquals(questionRequest.getContent(), result.getContent());
        assertEquals(questionRequest.getQuestionType(), result.getQuestionType());
        assertEquals(questionRequest.getOrderIndex(), result.getOrderIndex());
        
        verify(homeworkRepository).findById(testHomework.getId());
        verify(questionRepository).save(any(Question.class));
        verify(userRepository).findByUsername(TEST_USERNAME);
    }
}