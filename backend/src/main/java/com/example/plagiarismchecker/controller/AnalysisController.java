package com.example.plagiarismchecker.controller;

import com.example.plagiarismchecker.dto.ComparisonResponse;
import com.example.plagiarismchecker.dto.SimilarityResultResponse;
import com.example.plagiarismchecker.security.SecurityUtils;
import com.example.plagiarismchecker.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/assignments/{assignmentId}")
    public List<SimilarityResultResponse> analyze(@PathVariable Long assignmentId) {
        return analysisService.analyze(assignmentId, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/assignments/{assignmentId}")
    public List<SimilarityResultResponse> getResults(@PathVariable Long assignmentId) {
        return analysisService.getResults(assignmentId, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/{resultId}")
    public SimilarityResultResponse getById(@PathVariable Long resultId) {
        return analysisService.getById(resultId, SecurityUtils.getCurrentUser());
    }

    @GetMapping("/{resultId}/comparison")
    public ComparisonResponse getComparison(@PathVariable Long resultId) {
        return analysisService.getComparison(resultId, SecurityUtils.getCurrentUser());
    }
}
