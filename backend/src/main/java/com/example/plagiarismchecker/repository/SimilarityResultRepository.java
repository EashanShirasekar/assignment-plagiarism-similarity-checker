package com.example.plagiarismchecker.repository;

import com.example.plagiarismchecker.entity.SimilarityResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimilarityResultRepository extends JpaRepository<SimilarityResult, Long> {

    List<SimilarityResult> findByAssignmentIdOrderBySimilarityPercentageDesc(Long assignmentId);

    List<SimilarityResult> findByAssignmentIdAndFlaggedTrueOrderBySimilarityPercentageDesc(Long assignmentId);

    void deleteByAssignmentId(Long assignmentId);

    long countByAssignmentIdInAndFlaggedTrue(List<Long> assignmentIds);
}
