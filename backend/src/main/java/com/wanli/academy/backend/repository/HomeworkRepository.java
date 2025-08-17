package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 作业数据访问接口
 * 提供作业相关的数据库操作方法
 */
@Repository
public interface HomeworkRepository extends JpaRepository<Homework, UUID> {
    
    /**
     * 根据创建者ID查询作业列表
     * @param creatorId 创建者ID
     * @return 作业列表
     */
    List<Homework> findByCreatorId(Long creatorId);
    
    /**
     * 根据创建者ID查询作业列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @return 作业列表
     */
    List<Homework> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    
    /**
     * 根据标题查询作业（精确匹配）
     * @param title 作业标题
     * @return 作业对象
     */
    Optional<Homework> findByTitle(String title);
    
    /**
     * 根据标题模糊查询作业列表
     * @param title 作业标题关键词
     * @return 作业列表
     */
    List<Homework> findByTitleContainingIgnoreCase(String title);
    
    /**
     * 根据创建者ID和标题查询作业
     * @param creatorId 创建者ID
     * @param title 作业标题
     * @return 作业对象
     */
    Optional<Homework> findByCreatorIdAndTitle(Long creatorId, String title);
    
    /**
     * 检查指定创建者是否存在指定标题的作业
     * @param creatorId 创建者ID
     * @param title 作业标题
     * @return 是否存在
     */
    boolean existsByCreatorIdAndTitle(Long creatorId, String title);
    
    /**
     * 统计指定创建者的作业数量
     * @param creatorId 创建者ID
     * @return 作业数量
     */
    long countByCreatorId(Long creatorId);
    
    /**
     * 查询所有作业，按创建时间倒序排列
     * @return 作业列表
     */
    List<Homework> findAllByOrderByCreatedAtDesc();
    
    /**
     * 使用自定义查询根据创建者ID获取作业及其题目数量
     * @param creatorId 创建者ID
     * @return 作业列表及题目数量信息
     */
    @Query("SELECT h FROM Homework h WHERE h.creatorId = :creatorId ORDER BY h.createdAt DESC")
    List<Homework> findByCreatorIdWithQuestions(@Param("creatorId") Long creatorId);
}