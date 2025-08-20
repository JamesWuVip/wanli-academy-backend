package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId")
    long countByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.status = 'GRADED'")
    long countGradedByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT AVG(s.score) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.status = 'GRADED' AND s.score IS NOT NULL")
    Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
    
    List<AssignmentSubmission> findByStudentId(Long studentId);
    
    AssignmentSubmission findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}