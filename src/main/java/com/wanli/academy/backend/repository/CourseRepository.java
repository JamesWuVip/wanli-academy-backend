package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Course entity
 * Provides data access methods for courses
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find all courses created by a specific user
     * @param creatorId the creator user ID
     * @return list of courses
     */
    List<Course> findByCreatorId(Long creatorId);

    /**
     * Find courses by name (case-insensitive)
     * @param name the course name
     * @return list of courses
     */
    List<Course> findByNameContainingIgnoreCase(String name);

    /**
     * Find active courses
     * @param isActive the active status
     * @return list of active courses
     */
    List<Course> findByIsActive(Boolean isActive);

    /**
     * Find courses by creator and active status
     * @param creatorId the creator user ID
     * @param isActive the active status
     * @return list of courses
     */
    List<Course> findByCreatorIdAndIsActive(Long creatorId, Boolean isActive);

    /**
     * Count courses by creator
     * @param creatorId the creator user ID
     * @return count of courses
     */
    long countByCreatorId(Long creatorId);

    /**
     * Count active courses by creator
     * @param creatorId the creator user ID
     * @param isActive the active status
     * @return count of active courses
     */
    long countByCreatorIdAndIsActive(Long creatorId, Boolean isActive);

    /**
     * Find course by assignment ID
     * @param assignmentId the assignment ID
     * @return optional course
     */
    @Query("SELECT c FROM Course c JOIN Assignment a ON c.id = a.courseId WHERE a.id = :assignmentId")
    Optional<Course> findByAssignmentId(@Param("assignmentId") UUID assignmentId);

    /**
     * Check if course exists by name and creator
     * @param name the course name
     * @param creatorId the creator user ID
     * @return true if course exists
     */
    boolean existsByNameAndCreatorId(String name, Long creatorId);

    /**
     * Find courses with assignments count
     * @param creatorId the creator user ID
     * @return list of courses
     */
    @Query("SELECT c FROM Course c LEFT JOIN Assignment a ON c.id = a.courseId WHERE c.creatorId = :creatorId GROUP BY c.id")
    List<Course> findCoursesWithAssignmentsByCreatorId(@Param("creatorId") Long creatorId);
}