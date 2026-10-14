package com.example.plagiarismchecker.controller;

import com.example.plagiarismchecker.dto.SubmissionRequest;
import com.example.plagiarismchecker.dto.SubmissionResponse;
import com.example.plagiarismchecker.security.SecurityUtils;
import com.example.plagiarismchecker.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/api/submissions")
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse response = submissionService.submit(request, SecurityUtils.getCurrentUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/submissions/{id}")
    public ResponseEntity<SubmissionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getById(id, SecurityUtils.getCurrentUser()));
    }

    @GetMapping("/api/assignments/{id}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getByAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getByAssignment(id, SecurityUtils.getCurrentUser()));
    }
}
