package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Assignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 作业数据访问接口
 * 提供作业相关的数据库操作方法
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    
    /**
     * 根据创建者ID查询作业列表
     * @param creatorId 创建者ID
     * @return 作业列表
     */
    List<Assignment> findByCreatorId(Long creatorId);
    
    /**
     * 根据创建者ID查询作业列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @return 作业列表
     */
    List<Assignment> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    
    /**
     * 分页根据创建者ID查询作业列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);
    
    /**
     * 根据标题查询作业（精确匹配）
     * @param title 作业标题
     * @return 作业对象
     */
    Optional<Assignment> findByTitle(String title);
    
    /**
     * 根据标题模糊查询作业列表
     * @param title 作业标题关键词
     * @return 作业列表
     */
    List<Assignment> findByTitleContainingIgnoreCase(String title);
    
    /**
     * 分页根据标题模糊查询作业列表，按创建时间倒序排列
     * @param title 作业标题关键词
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);
    
    /**
     * 分页根据创建者ID和标题模糊查询作业列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @param title 作业标题关键词
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByCreatorIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(Long creatorId, String title, Pageable pageable);
    
    /**
     * 分页根据状态和标题模糊查询作业列表，按创建时间倒序排列
     * @param status 作业状态
     * @param title 作业标题关键词
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String status, String title, Pageable pageable);
    
    /**
     * 分页根据创建者ID、状态和标题模糊查询作业列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @param status 作业状态
     * @param title 作业标题关键词
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByCreatorIdAndStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(Long creatorId, String status, String title, Pageable pageable);
    
    /**
     * 分页查询所有作业，按创建时间倒序排列
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据创建者ID和标题查询作业
     * @param creatorId 创建者ID
     * @param title 作业标题
     * @return 作业对象
     */
    Optional<Assignment> findByCreatorIdAndTitle(Long creatorId, String title);
    
    /**
     * 根据状态查询作业列表
     * @param status 作业状态
     * @return 作业列表
     */
    List<Assignment> findByStatus(String status);
    
    /**
     * 根据状态查询作业列表，按创建时间倒序排列
     * @param status 作业状态
     * @return 作业列表
     */
    List<Assignment> findByStatusOrderByCreatedAtDesc(String status);
    
    /**
     * 分页根据状态查询作业列表，按创建时间倒序排列
     * @param status 作业状态
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * 根据创建者ID和状态查询作业列表
     * @param creatorId 创建者ID
     * @param status 作业状态
     * @return 作业列表
     */
    List<Assignment> findByCreatorIdAndStatus(Long creatorId, String status);
    
    /**
     * 分页根据创建者ID和状态查询作业列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @param status 作业状态
     * @param pageable 分页参数
     * @return 分页作业列表
     */
    Page<Assignment> findByCreatorIdAndStatusOrderByCreatedAtDesc(Long creatorId, String status, Pageable pageable);
    
    /**
     * 查询截止日期在指定时间之前的作业
     * @param dueDate 截止日期
     * @return 作业列表
     */
    List<Assignment> findByDueDateBefore(LocalDateTime dueDate);
    
    /**
     * 查询截止日期在指定时间之后的作业
     * @param dueDate 截止日期
     * @return 作业列表
     */
    List<Assignment> findByDueDateAfter(LocalDateTime dueDate);
    
    /**
     * 查询截止日期在指定时间范围内的作业
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 作业列表
     */
    List<Assignment> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
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
     * 统计指定状态的作业数量
     * @param status 作业状态
     * @return 作业数量
     */
    long countByStatus(String status);
    
    /**
     * 查询已发布的作业列表，按创建时间倒序排列
     * @return 作业列表
     */
    @Query("SELECT DISTINCT a FROM Assignment a LEFT JOIN FETCH a.submissions WHERE a.status = 'PUBLISHED' ORDER BY a.createdAt DESC")
    List<Assignment> findPublishedAssignments();
    
    /**
     * 查询即将到期的作业列表（截止日期在指定小时数内）
     * @param hours 小时数
     * @return 作业列表
     */
    @Query(value = "SELECT * FROM assignments WHERE status = 'PUBLISHED' AND due_date BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL ':hours hour'", nativeQuery = true)
    List<Assignment> findAssignmentsDueSoon(@Param("hours") int hours);
    
    /**
     * 查询已过期但未关闭的作业列表
     * @return 作业列表
     */
    @Query(value = "SELECT * FROM assignments WHERE status != 'CLOSED' AND due_date < CURRENT_TIMESTAMP", nativeQuery = true)
    List<Assignment> findOverdueAssignments();
    
    /**
     * 根据创建者ID和状态统计作业数量
     * @param creatorId 创建者ID
     * @param status 作业状态
     * @return 作业数量
     */
    long countByCreatorIdAndStatus(Long creatorId, String status);
}