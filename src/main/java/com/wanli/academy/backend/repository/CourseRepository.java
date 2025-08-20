package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 课程数据访问接口
 * 提供课程相关的数据库操作方法
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    
    /**
     * 根据创建者ID查询课程列表
     * @param creatorId 创建者ID
     * @return 课程列表
     */
    List<Course> findByCreatorId(Long creatorId);
    
    /**
     * 根据创建者ID查询课程列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @return 课程列表
     */
    List<Course> findByCreatorIdOrderByCreatedAtDesc(Long creatorId);
    
    /**
     * 分页根据创建者ID查询课程列表，按创建时间倒序排列
     * @param creatorId 创建者ID
     * @param pageable 分页参数
     * @return 分页课程列表
     */
    Page<Course> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);
    
    /**
     * 根据课程名称查询课程（精确匹配）
     * @param name 课程名称
     * @return 课程对象
     */
    Optional<Course> findByName(String name);
    
    /**
     * 根据课程名称模糊查询课程列表（忽略大小写）
     * @param name 课程名称关键字
     * @return 课程列表
     */
    List<Course> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据激活状态查询课程列表
     * @param isActive 是否激活
     * @return 课程列表
     */
    List<Course> findByIsActive(Boolean isActive);
    
    /**
     * 根据创建者ID和激活状态查询课程列表
     * @param creatorId 创建者ID
     * @param isActive 是否激活
     * @return 课程列表
     */
    List<Course> findByCreatorIdAndIsActive(Long creatorId, Boolean isActive);
    
    /**
     * 根据创建者ID统计课程数量
     * @param creatorId 创建者ID
     * @return 课程数量
     */
    long countByCreatorId(Long creatorId);
    
    /**
     * 根据激活状态统计课程数量
     * @param isActive 是否激活
     * @return 课程数量
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * 根据作业ID查询所属课程
     * @param assignmentId 作业ID
     * @return 课程对象
     */
    @Query("SELECT c FROM Course c JOIN c.assignments a WHERE a.id = :assignmentId")
    Optional<Course> findByAssignmentId(@Param("assignmentId") UUID assignmentId);
}