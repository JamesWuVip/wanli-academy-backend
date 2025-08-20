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
 * Repository interface for AssignmentSubmission entity
 * Provides data access methods for assignment submissions
 */
@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, UUID> {

    /**
     * Find all submissions for a specific assignment
     * @param assignmentId the assignment ID
     * @return list of submissions
     */
    List<AssignmentSubmission> findByAssignmentId(UUID assignmentId);

    /**
     * Find all submissions by a specific student
     * @param studentId the student ID
     * @return list of submissions
     */
    List<AssignmentSubmission> findByStudentId(Long studentId);

    /**
     * Find submission by assignment and student
     * @param assignmentId the assignment ID
     * @param studentId the student ID
     * @return optional submission
     */
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, Long studentId);

    /**
     * Find submissions by status
     * @param status the submission status
     * @return list of submissions
     */
    List<AssignmentSubmission> findByStatus(String status);

    /**
     * Count submissions for a specific assignment
     * @param assignmentId the assignment ID
     * @return count of submissions
     */
    long countByAssignmentId(UUID assignmentId);

    /**
     * Count graded submissions for a specific assignment
     * @param assignmentId the assignment ID
     * @return count of graded submissions
     */
    @Query("SELECT COUNT(s) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    long countGradedByAssignmentId(@Param("assignmentId") UUID assignmentId);

    /**
     * Calculate average score for a specific assignment
     * @param assignmentId the assignment ID
     * @return average score
     */
    @Query("SELECT AVG(s.score) FROM AssignmentSubmission s WHERE s.assignmentId = :assignmentId AND s.score IS NOT NULL")
    Double findAverageScoreByAssignmentId(@Param("assignmentId") UUID assignmentId);

    /**
     * Find submissions by assignment and status
     * @param assignmentId the assignment ID
     * @param status the submission status
     * @return list of submissions
     */
    List<AssignmentSubmission> findByAssignmentIdAndStatus(UUID assignmentId, String status);

    /**
     * Check if student has submitted for assignment
     * @param assignmentId the assignment ID
     * @param studentId the student ID
     * @return true if submission exists
     */
    boolean existsByAssignmentIdAndStudentId(UUID assignmentId, Long studentId);
}