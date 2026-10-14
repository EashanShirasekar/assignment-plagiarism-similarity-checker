package com.example.plagiarismchecker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createdAt;
    private int submissionCount;
    private boolean submittedByCurrentStudent;
}
