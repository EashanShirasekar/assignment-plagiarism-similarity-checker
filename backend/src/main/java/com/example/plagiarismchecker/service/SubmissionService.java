package com.example.plagiarismchecker.service;

import com.example.plagiarismchecker.dto.SubmissionRequest;
import com.example.plagiarismchecker.dto.SubmissionResponse;
import com.example.plagiarismchecker.entity.Assignment;
import com.example.plagiarismchecker.entity.Role;
import com.example.plagiarismchecker.entity.Submission;
import com.example.plagiarismchecker.entity.User;
import com.example.plagiarismchecker.exception.BadRequestException;
import com.example.plagiarismchecker.exception.ResourceNotFoundException;
import com.example.plagiarismchecker.exception.UnauthorizedException;
import com.example.plagiarismchecker.repository.SubmissionRepository;
import com.example.plagiarismchecker.repository.UserRepository;
import com.example.plagiarismchecker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final AssignmentService assignmentService;

    @Transactional
    public SubmissionResponse submit(SubmissionRequest request, UserPrincipal currentUser) {
        if (!Role.STUDENT.name().equals(currentUser.getRole())) {
            throw new UnauthorizedException("Only students can submit assignments");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BadRequestException("Submission content cannot be empty");
        }

        Assignment assignment = assignmentService.findAssignmentOrThrow(request.getAssignmentId());

        if (assignment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("The deadline for this assignment has passed");
        }

        if (submissionRepository.existsByAssignmentIdAndStudentId(assignment.getId(), currentUser.getId())) {
            throw new BadRequestException("You have already submitted this assignment");
        }

        User student = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(request.getContent().trim())
                .build();

        Submission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    public SubmissionResponse getById(Long id, UserPrincipal currentUser) {
        Submission submission = findSubmissionOrThrow(id);

        boolean isOwner = submission.getStudent().getId().equals(currentUser.getId());
        boolean isAssignmentTeacher = submission.getAssignment().getTeacher().getId().equals(currentUser.getId());

        if (!isOwner && !isAssignmentTeacher) {
            throw new UnauthorizedException("You do not have access to this submission");
        }

        return toResponse(submission);
    }

    public List<SubmissionResponse> getByAssignment(Long assignmentId, UserPrincipal currentUser) {
        Assignment assignment = assignmentService.findAssignmentOrThrow(assignmentId);
        assignmentService.requireOwnership(assignment, currentUser);

        return submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(assignmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    Submission findSubmissionOrThrow(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));
    }

    private SubmissionResponse toResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .content(submission.getContent())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
