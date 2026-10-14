package com.example.plagiarismchecker.repository;

import com.example.plagiarismchecker.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentIdOrderBySubmittedAtAsc(Long assignmentId);
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<Submission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);
    long countByAssignmentId(Long assignmentId);
    long countByAssignmentIdIn(List<Long> assignmentIds);
}
