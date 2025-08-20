package com.wanli.academy.backend.repository;

import com.wanli.academy.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByCreatorId(Long creatorId);
    
    List<Course> findByStatus(String status);
    
    List<Course> findByCreatorIdAndStatus(Long creatorId, String status);
}