package com.example.plagiarismchecker.repository;

import com.example.plagiarismchecker.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
    List<Assignment> findAllByOrderByCreatedAtDesc();
}
