package com.example.plagiarismchecker.dto;

import com.example.plagiarismchecker.entity.SimilarityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SimilarityResultResponse {
    private Long id;
    private Long assignmentId;
    private Long submissionId1;
    private String student1Name;
    private Long submissionId2;
    private String student2Name;
    private double similarityPercentage;
    private SimilarityLevel similarityLevel;
    private boolean flagged;
    private LocalDateTime createdAt;
}
