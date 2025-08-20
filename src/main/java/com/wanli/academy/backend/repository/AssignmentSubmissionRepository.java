package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 作业提交数据访问接口
 * 提供作业提交相关的数据库操作方法
 */
@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {
    
    /**
     * 根据作业ID查询提交记录列表
     * @param assignmentId 作业ID
     * @return 提交记录列表
     */
    List<AssignmentSubmission> findByAssignmentId(UUID assignmentId);
    
    /**
     * 根据学生ID查询提交记录列表
     * @param studentId 学生ID
     * @return 提交记录列表
     */
    List<AssignmentSubmission> findByStudentId(Long studentId);
    
    /**
     * 根据作业ID和学生ID查询提交记录
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 提交记录
     */
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, Long studentId);
    
    /**
     * 根据状态查询提交记录列表
     * @param status 提交状态
     * @return 提交记录列表
     */
    List<AssignmentSubmission> findByStatus(String status);
    
    /**
     * 根据作业ID统计提交数量
     * @param assignmentId 作业ID
     * @return 提交数量
     */
    long countByAssignmentId(UUID assignmentId);
    
    /**
     * 根据作业ID和状态统计提交数量
     * @param assignmentId 作业ID
     * @param status 提交状态
     * @return 提交数量
     */
    long countByAssignmentIdAndStatus(UUID assignmentId, String status);
    
    /**
     * 根据作业ID计算平均分
     * @param assignmentId 作业ID
     * @return 平均分
     */
    @Query("SELECT AVG(s.score) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Double findAverageScoreByAssignmentId(@Param("assignmentId") UUID assignmentId);
    
    /**
     * 查询待批改的提交记录
     * @return 待批改的提交记录列表
     */
    @Query("SELECT s FROM AssignmentSubmission s WHERE s.status = 'SUBMITTED' ORDER BY s.submittedAt ASC")
    List<AssignmentSubmission> findPendingGradeSubmissions();
}