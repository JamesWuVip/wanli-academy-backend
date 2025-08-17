package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 题目数据访问接口
 * 提供题目相关的数据库操作方法
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    
    /**
     * 根据作业ID查询题目列表，按顺序排列
     * @param homeworkId 作业ID
     * @return 题目列表
     */
    List<Question> findByHomeworkIdOrderByOrderIndexAsc(UUID homeworkId);
    
    /**
     * 根据作业ID查询题目列表
     * @param homeworkId 作业ID
     * @return 题目列表
     */
    List<Question> findByHomeworkId(UUID homeworkId);
    
    /**
     * 根据题型查询题目列表
     * @param questionType 题型
     * @return 题目列表
     */
    List<Question> findByQuestionType(String questionType);
    
    /**
     * 根据作业ID和题型查询题目列表
     * @param homeworkId 作业ID
     * @param questionType 题型
     * @return 题目列表
     */
    List<Question> findByHomeworkIdAndQuestionType(UUID homeworkId, String questionType);
    
    /**
     * 查询指定作业中题目的最大顺序号
     * @param homeworkId 作业ID
     * @return 最大顺序号，如果没有题目则返回null
     */
    @Query("SELECT MAX(q.orderIndex) FROM Question q WHERE q.homeworkId = :homeworkId")
    Optional<Integer> findMaxOrderIndexByHomeworkId(@Param("homeworkId") UUID homeworkId);
    
    /**
     * 统计指定作业的题目数量
     * @param homeworkId 作业ID
     * @return 题目数量
     */
    long countByHomeworkId(UUID homeworkId);
    
    /**
     * 统计指定题型的题目数量
     * @param questionType 题型
     * @return 题目数量
     */
    long countByQuestionType(String questionType);
    
    /**
     * 根据作业ID和顺序号查询题目
     * @param homeworkId 作业ID
     * @param orderIndex 顺序号
     * @return 题目对象
     */
    Optional<Question> findByHomeworkIdAndOrderIndex(UUID homeworkId, Integer orderIndex);
    
    /**
     * 检查指定作业中是否存在指定顺序号的题目
     * @param homeworkId 作业ID
     * @param orderIndex 顺序号
     * @return 是否存在
     */
    boolean existsByHomeworkIdAndOrderIndex(UUID homeworkId, Integer orderIndex);
    
    /**
     * 删除指定作业的所有题目
     * @param homeworkId 作业ID
     */
    void deleteByHomeworkId(UUID homeworkId);
    
    /**
     * 查询指定作业中顺序号大于指定值的题目
     * @param homeworkId 作业ID
     * @param orderIndex 顺序号
     * @return 题目列表
     */
    List<Question> findByHomeworkIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(UUID homeworkId, Integer orderIndex);
}