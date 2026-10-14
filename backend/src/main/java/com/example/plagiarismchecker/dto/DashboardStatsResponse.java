package com.example.plagiarismchecker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalAssignments;
    private long totalSubmissions;
    private long flaggedPairs;
    private double highestSimilarity;
}
