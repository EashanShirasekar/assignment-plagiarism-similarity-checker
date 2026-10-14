package com.example.plagiarismchecker.service;

import com.example.plagiarismchecker.dto.AssignmentRequest;
import com.example.plagiarismchecker.dto.AssignmentResponse;
import com.example.plagiarismchecker.entity.Assignment;
import com.example.plagiarismchecker.entity.Role;
import com.example.plagiarismchecker.entity.User;
import com.example.plagiarismchecker.exception.ResourceNotFoundException;
import com.example.plagiarismchecker.exception.UnauthorizedException;
import com.example.plagiarismchecker.repository.AssignmentRepository;
import com.example.plagiarismchecker.repository.SubmissionRepository;
import com.example.plagiarismchecker.repository.UserRepository;
import com.example.plagiarismchecker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public AssignmentResponse create(AssignmentRequest request, UserPrincipal currentUser) {
        requireTeacher(currentUser);

        User teacher = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Assignment assignment = Assignment.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .deadline(request.getDeadline())
                .teacher(teacher)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved, currentUser);
    }

    public List<AssignmentResponse> getAll(UserPrincipal currentUser) {
        List<Assignment> assignments = "TEACHER".equals(currentUser.getRole())
                ? assignmentRepository.findByTeacherIdOrderByCreatedAtDesc(currentUser.getId())
                : assignmentRepository.findAllByOrderByCreatedAtDesc();

        return assignments.stream().map(a -> toResponse(a, currentUser)).toList();
    }

    public AssignmentResponse getById(Long id, UserPrincipal currentUser) {
        Assignment assignment = findAssignmentOrThrow(id);
        return toResponse(assignment, currentUser);
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request, UserPrincipal currentUser) {
        requireTeacher(currentUser);
        Assignment assignment = findAssignmentOrThrow(id);
        requireOwnership(assignment, currentUser);

        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(request.getDescription().trim());
        assignment.setDeadline(request.getDeadline());

        Assignment saved = assignmentRepository.save(assignment);
        return toResponse(saved, currentUser);
    }

    @Transactional
    public void delete(Long id, UserPrincipal currentUser) {
        requireTeacher(currentUser);
        Assignment assignment = findAssignmentOrThrow(id);
        requireOwnership(assignment, currentUser);
        assignmentRepository.delete(assignment);
    }

    /** Package-private so AnalysisService/SubmissionService can reuse it without duplicating lookup logic. */
    Assignment findAssignmentOrThrow(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
    }

    void requireOwnership(Assignment assignment, UserPrincipal currentUser) {
        if (!assignment.getTeacher().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only manage assignments you created");
        }
    }

    private void requireTeacher(UserPrincipal currentUser) {
        if (!Role.TEACHER.name().equals(currentUser.getRole())) {
            throw new UnauthorizedException("Only teachers can perform this action");
        }
    }

    private AssignmentResponse toResponse(Assignment assignment, UserPrincipal currentUser) {
        int submissionCount = (int) submissionRepository.countByAssignmentId(assignment.getId());

        boolean submittedByCurrentStudent = "STUDENT".equals(currentUser.getRole())
                && submissionRepository.existsByAssignmentIdAndStudentId(assignment.getId(), currentUser.getId());

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .deadline(assignment.getDeadline())
                .teacherId(assignment.getTeacher().getId())
                .teacherName(assignment.getTeacher().getName())
                .createdAt(assignment.getCreatedAt())
                .submissionCount(submissionCount)
                .submittedByCurrentStudent(submittedByCurrentStudent)
                .build();
    }
}
