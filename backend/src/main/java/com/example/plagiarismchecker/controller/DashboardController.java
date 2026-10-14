package com.example.plagiarismchecker.controller;

import com.example.plagiarismchecker.dto.DashboardStatsResponse;
import com.example.plagiarismchecker.security.SecurityUtils;
import com.example.plagiarismchecker.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalysisService analysisService;

    @GetMapping("/teacher")
    public DashboardStatsResponse getTeacherStats() {
        return analysisService.getDashboardStats(SecurityUtils.getCurrentUser());
    }
}
