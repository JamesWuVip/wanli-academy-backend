package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 作业提交数据访问接口
 * 提供作业提交相关的数据库操作方法
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    
    /**
     * 根据作业ID查询提交记录列表
     * @param assignmentId 作业ID
     * @return 提交记录列表
     */
    List<Submission> findByAssignmentId(UUID assignmentId);
    
    /**
     * 根据学生ID查询提交记录列表
     * @param studentId 学生ID
     * @return 提交记录列表
     */
    List<Submission> findByStudentId(Long studentId);
    
    /**
     * 根据作业ID和学生ID查询提交记录
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 提交记录
     */
    Optional<Submission> findByAssignmentIdAndStudentId(UUID assignmentId, Long studentId);
    
    /**
     * 根据状态查询提交记录列表
     * @param status 提交状态
     * @return 提交记录列表
     */
    List<Submission> findByStatus(String status);
    
    /**
     * 根据作业ID和状态查询提交记录列表
     * @param assignmentId 作业ID
     * @param status 提交状态
     * @return 提交记录列表
     */
    List<Submission> findByAssignmentIdAndStatus(UUID assignmentId, String status);
    
    /**
     * 根据学生ID和状态查询提交记录列表
     * @param studentId 学生ID
     * @param status 提交状态
     * @return 提交记录列表
     */
    List<Submission> findByStudentIdAndStatus(Long studentId, String status);
    
    /**
     * 根据批改教师ID查询提交记录列表
     * @param graderId 批改教师ID
     * @return 提交记录列表
     */
    List<Submission> findByGradedBy(Long graderId);
    
    /**
     * 根据作业ID查询提交记录列表，按提交时间倒序排列
     * @param assignmentId 作业ID
     * @return 提交记录列表
     */
    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(UUID assignmentId);
    
    /**
     * 根据学生ID查询提交记录列表，按提交时间倒序排列
     * @param studentId 学生ID
     * @return 提交记录列表
     */
    List<Submission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);
    
    /**
     * 查询提交时间在指定时间之后的提交记录
     * @param submittedAt 提交时间
     * @return 提交记录列表
     */
    List<Submission> findBySubmittedAtAfter(LocalDateTime submittedAt);
    
    /**
     * 查询提交时间在指定时间之前的提交记录
     * @param submittedAt 提交时间
     * @return 提交记录列表
     */
    List<Submission> findBySubmittedAtBefore(LocalDateTime submittedAt);
    
    /**
     * 查询提交时间在指定时间范围内的提交记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 提交记录列表
     */
    List<Submission> findBySubmittedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 检查指定学生是否已提交指定作业
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 是否已提交
     */
    boolean existsByAssignmentIdAndStudentId(UUID assignmentId, Long studentId);
    
    /**
     * 统计指定作业的提交数量
     * @param assignmentId 作业ID
     * @return 提交数量
     */
    long countByAssignmentId(UUID assignmentId);
    
    /**
     * 统计指定学生的提交数量
     * @param studentId 学生ID
     * @return 提交数量
     */
    long countByStudentId(Long studentId);
    
    /**
     * 统计指定状态的提交数量
     * @param status 提交状态
     * @return 提交数量
     */
    long countByStatus(String status);
    
    /**
     * 统计指定作业和状态的提交数量
     * @param assignmentId 作业ID
     * @param status 提交状态
     * @return 提交数量
     */
    long countByAssignmentIdAndStatus(UUID assignmentId, String status);
    
    /**
     * 查询待批改的提交记录列表
     * @return 提交记录列表
     */
    @Query("SELECT s FROM Submission s WHERE s.status = 'SUBMITTED' ORDER BY s.submittedAt ASC")
    List<Submission> findPendingGradeSubmissions();
    
    /**
     * 查询指定教师需要批改的提交记录列表
     * @param teacherId 教师ID
     * @return 提交记录列表
     */
    @Query("SELECT s FROM Submission s JOIN s.assignment a WHERE a.creatorId = :teacherId AND s.status = 'SUBMITTED' ORDER BY s.submittedAt ASC")
    List<Submission> findSubmissionsToGradeByTeacher(@Param("teacherId") Long teacherId);
    
    /**
     * 查询指定作业的平均分
     * @param assignmentId 作业ID
     * @return 平均分
     */
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Double findAverageScoreByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    /**
     * 查询指定学生的平均分
     * @param studentId 学生ID
     * @return 平均分
     */
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.studentId = :studentId AND s.score IS NOT NULL")
    Double findAverageScoreByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询指定作业的最高分
     * @param assignmentId 作业ID
     * @return 最高分
     */
    @Query("SELECT MAX(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Integer findMaxScoreByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    /**
     * 查询指定作业的最低分
     * @param assignmentId 作业ID
     * @return 最低分
     */
    @Query("SELECT MIN(s.score) FROM Submission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Integer findMinScoreByAssignmentId(@Param("assignmentId") UUID assignmentId);
}