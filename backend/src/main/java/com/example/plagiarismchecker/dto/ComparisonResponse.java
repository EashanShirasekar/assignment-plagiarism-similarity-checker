package com.example.plagiarismchecker.dto;

import com.example.plagiarismchecker.entity.SimilarityLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ComparisonResponse {
    private Long resultId;
    private Long assignmentId;
    private String assignmentTitle;

    private Long submissionId1;
    private String student1Name;
    private String content1;

    private Long submissionId2;
    private String student2Name;
    private String content2;

    private double similarityPercentage;
    private SimilarityLevel similarityLevel;
    private boolean flagged;

    /** Significant words found in both submissions after preprocessing - used by the
     *  frontend to highlight overlapping portions of text. Purely informational. */
    private List<String> commonSignificantTerms;

    private String disclaimer;
}
