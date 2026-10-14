package com.example.plagiarismchecker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionRequest {

    @NotNull(message = "Assignment id is required")
    private Long assignmentId;

    @NotBlank(message = "Submission content cannot be empty")
    private String content;
}
