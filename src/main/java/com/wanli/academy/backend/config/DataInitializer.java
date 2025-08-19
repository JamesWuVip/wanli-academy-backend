package com.wanli.academy.backend.config;

import com.wanli.academy.backend.entity.Role;
import com.wanli.academy.backend.entity.Question;
import com.wanli.academy.backend.entity.Assignment;
import com.wanli.academy.backend.entity.Submission;
import com.wanli.academy.backend.repository.RoleRepository;
import com.wanli.academy.backend.repository.QuestionRepository;
import com.wanli.academy.backend.repository.AssignmentRepository;
import com.wanli.academy.backend.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 数据初始化器
 * 在应用启动时初始化基础数据，包括角色等
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("开始初始化基础数据...");
        initializeRoles();
        initializeVideoData();
        initializeSubmissionData();
        logger.info("基础数据初始化完成");
    }
    
    /**
     * 初始化角色数据
     */
    private void initializeRoles() {
        logger.info("初始化角色数据...");
        
        // 创建基础角色
        createRoleIfNotExists("ROLE_ADMIN", "系统管理员，拥有所有权限");
        createRoleIfNotExists("ROLE_STUDENT", "学生用户，可以查看和提交作业");
        
        // Sprint 2 新增角色
        createRoleIfNotExists("ROLE_HQ_TEACHER", "总部教师，可以创建和管理作业");
        createRoleIfNotExists("ROLE_FRANCHISE_TEACHER", "加盟商教师，可以查看和批阅作业");
        
        logger.info("角色数据初始化完成");
    }
    
    /**
     * 初始化视频数据
     * 为现有的Question记录添加视频URL和解析内容
     */
    private void initializeVideoData() {
        logger.info("初始化视频数据...");
        
        List<Question> questions = questionRepository.findAll();
        if (questions.isEmpty()) {
            logger.info("没有找到Question记录，跳过视频数据初始化");
            return;
        }
        
        // 准备视频URL和解析内容的映射
        Map<String, VideoData> videoDataMap = prepareVideoData();
        
        int updatedCount = 0;
         for (Question question : questions) {
             if (question.getVideoUrl() == null || question.getVideoUrl().trim().isEmpty()) {
                 Map<String, Object> contentMap = question.getContent();
                 String contentText = extractTextFromContent(contentMap);
                 VideoData videoData = getVideoDataForContent(contentText, videoDataMap);
                 
                 if (videoData != null) {
                     question.setVideoUrl(videoData.videoUrl);
                     question.setExplanation(videoData.explanation);
                     questionRepository.save(question);
                     updatedCount++;
                     logger.info("更新Question视频数据: ID={}, VideoURL={}", question.getId(), videoData.videoUrl);
                 }
             }
         }
        
        logger.info("视频数据初始化完成，更新了{}个Question记录", updatedCount);
    }
    
    /**
     * 准备视频数据映射
     */
    private Map<String, VideoData> prepareVideoData() {
        Map<String, VideoData> videoDataMap = new HashMap<>();
        
        // 数学题目视频
        videoDataMap.put("math", new VideoData(
            "https://www.youtube.com/embed/dQw4w9WgXcQ",
            "这是一道基础数学题目。首先我们需要理解加法的概念：1 + 1 表示将1个单位与另外1个单位相加。根据数学基本运算规则，1 + 1 = 2。这是最基础的算术运算，是学习更复杂数学概念的基础。"
        ));
        
        // 语文题目视频
        videoDataMap.put("chinese", new VideoData(
            "https://www.youtube.com/embed/kJQP7kiw5Fk",
            "环境保护是当今社会面临的重要议题。我的看法是：环境保护不仅关系到我们当代人的生活质量，更关系到子孙后代的生存环境。具体保护措施包括：1. 减少使用一次性塑料制品，选择可重复使用的环保材料；2. 积极参与垃圾分类，提高资源回收利用率；3. 选择绿色出行方式，如步行、骑自行车或使用公共交通工具，减少碳排放。"
        ));
        
        return videoDataMap;
    }
    
    /**
      * 从Question的content Map中提取文本内容
      */
     private String extractTextFromContent(Map<String, Object> contentMap) {
         if (contentMap == null) {
             return null;
         }
         
         // 尝试从不同的字段中提取文本
         Object textObj = contentMap.get("text");
         if (textObj != null) {
             return textObj.toString();
         }
         
         Object questionObj = contentMap.get("question");
         if (questionObj != null) {
             return questionObj.toString();
         }
         
         // 如果都没有，返回整个Map的字符串表示
         return contentMap.toString();
     }
     
     /**
      * 根据题目内容获取对应的视频数据
      */
     private VideoData getVideoDataForContent(String content, Map<String, VideoData> videoDataMap) {
         if (content == null) {
             return null;
         }
         
         // 判断是数学题目还是语文题目
         if (content.contains("1 + 1") || content.contains("+") || content.contains("数学") || content.contains("苹果") || content.contains("÷")) {
             return videoDataMap.get("math");
         } else if (content.contains("环境保护") || content.contains("看法") || content.contains("措施")) {
             return videoDataMap.get("chinese");
         }
         
         // 默认返回数学视频
         return videoDataMap.get("math");
     }
    
    /**
     * 视频数据内部类
     */
    private static class VideoData {
        public final String videoUrl;
        public final String explanation;
        
        public VideoData(String videoUrl, String explanation) {
            this.videoUrl = videoUrl;
            this.explanation = explanation;
        }
    }
    
    /**
     * 初始化提交数据
     * 创建一些GRADED状态的提交记录用于测试
     */
    private void initializeSubmissionData() {
        logger.info("初始化提交数据...");
        
        // 检查是否已有提交数据
        if (submissionRepository.count() > 0) {
            logger.info("提交数据已存在，跳过初始化");
            return;
        }
        
        // 获取现有的作业
        List<Assignment> assignments = assignmentRepository.findAll();
        if (assignments.isEmpty()) {
            logger.info("没有找到作业记录，跳过提交数据初始化");
            return;
        }
        
        // 为第一个作业创建一个已批阅的提交记录
        Assignment firstAssignment = assignments.get(0);
        
        Submission gradedSubmission = new Submission();
         gradedSubmission.setId(UUID.randomUUID());
         gradedSubmission.setAssignmentId(firstAssignment.getId());
         gradedSubmission.setStudentId(3L); // 假设学生ID为3
         gradedSubmission.setContent("这是学生提交的作业内容，包含了对题目的详细回答。");
        gradedSubmission.setStatus("GRADED");
        gradedSubmission.setScore(85);
        gradedSubmission.setFeedback("作业完成得很好，思路清晰，答案正确。建议在细节方面再加强一些。");
        gradedSubmission.setSubmittedAt(LocalDateTime.now().minusDays(2));
        gradedSubmission.setGradedAt(LocalDateTime.now().minusDays(1));
        gradedSubmission.setGradedBy(1L); // 假设教师ID为1
        gradedSubmission.setCreatedAt(LocalDateTime.now().minusDays(2));
        gradedSubmission.setUpdatedAt(LocalDateTime.now().minusDays(1));
        
        submissionRepository.save(gradedSubmission);
        logger.info("创建已批阅提交记录: AssignmentID={}, SubmissionID={}", 
                   firstAssignment.getId(), gradedSubmission.getId());
        
        // 如果有多个作业，再创建一个提交记录
        if (assignments.size() > 1) {
            Assignment secondAssignment = assignments.get(1);
            
            Submission anotherSubmission = new Submission();
             anotherSubmission.setId(UUID.randomUUID());
             anotherSubmission.setAssignmentId(secondAssignment.getId());
             anotherSubmission.setStudentId(3L);
             anotherSubmission.setContent("第二份作业的提交内容，展示了学生对问题的理解和分析。");
            anotherSubmission.setStatus("GRADED");
            anotherSubmission.setScore(92);
            anotherSubmission.setFeedback("优秀的作业！分析透彻，逻辑清晰，表达准确。");
            anotherSubmission.setSubmittedAt(LocalDateTime.now().minusDays(1));
            anotherSubmission.setGradedAt(LocalDateTime.now());
            anotherSubmission.setGradedBy(1L);
            anotherSubmission.setCreatedAt(LocalDateTime.now().minusDays(1));
            anotherSubmission.setUpdatedAt(LocalDateTime.now());
            
            submissionRepository.save(anotherSubmission);
            logger.info("创建第二个已批阅提交记录: AssignmentID={}, SubmissionID={}", 
                       secondAssignment.getId(), anotherSubmission.getId());
        }
        
        logger.info("提交数据初始化完成");
    }
    
    /**
     * 如果角色不存在则创建
     * @param roleName 角色名称
     * @param description 角色描述
     */
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role(roleName, description);
            roleRepository.save(role);
            logger.info("创建角色: {} - {}", roleName, description);
        } else {
            logger.debug("角色已存在: {}", roleName);
        }
    }
}