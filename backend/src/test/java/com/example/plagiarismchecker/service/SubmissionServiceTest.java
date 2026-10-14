package com.example.plagiarismchecker.service;

import com.example.plagiarismchecker.dto.SubmissionRequest;
import com.example.plagiarismchecker.dto.SubmissionResponse;
import com.example.plagiarismchecker.entity.Assignment;
import com.example.plagiarismchecker.entity.Role;
import com.example.plagiarismchecker.entity.Submission;
import com.example.plagiarismchecker.entity.User;
import com.example.plagiarismchecker.exception.BadRequestException;
import com.example.plagiarismchecker.exception.UnauthorizedException;
import com.example.plagiarismchecker.repository.SubmissionRepository;
import com.example.plagiarismchecker.repository.UserRepository;
import com.example.plagiarismchecker.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignmentService assignmentService;

    @InjectMocks
    private SubmissionService submissionService;

    private User studentEntity;
    private UserPrincipal studentPrincipal;
    private UserPrincipal teacherPrincipal;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        studentEntity = User.builder().id(2L).name("Rahul").email("student1@college.edu").password("hash").role(Role.STUDENT).build();
        User teacherEntity = User.builder().id(1L).name("Dr. Mehta").email("teacher1@college.edu").password("hash").role(Role.TEACHER).build();

        studentPrincipal = new UserPrincipal(studentEntity);
        teacherPrincipal = new UserPrincipal(teacherEntity);

        assignment = Assignment.builder().id(10L).title("OOP").description("desc")
                .deadline(LocalDateTime.now().plusDays(3)).teacher(teacherEntity).build();
    }

    @Test
    void studentCanSubmitOnce() {
        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(10L);
        request.setContent("Java is an object oriented programming language.");

        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.existsByAssignmentIdAndStudentId(10L, 2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentEntity));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission s = invocation.getArgument(0);
            s.setId(500L);
            s.setSubmittedAt(LocalDateTime.now());
            return s;
        });

        SubmissionResponse response = submissionService.submit(request, studentPrincipal);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getStudentId()).isEqualTo(2L);
    }

    @Test
    void studentCannotSubmitTwiceToSameAssignment() {
        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(10L);
        request.setContent("Some content");

        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.existsByAssignmentIdAndStudentId(10L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> submissionService.submit(request, studentPrincipal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void emptySubmissionContentIsRejected() {
        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(10L);
        request.setContent("   ");

        assertThatThrownBy(() -> submissionService.submit(request, studentPrincipal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void teacherCannotSubmitAssignment() {
        SubmissionRequest request = new SubmissionRequest();
        request.setAssignmentId(10L);
        request.setContent("Some content");

        assertThatThrownBy(() -> submissionService.submit(request, teacherPrincipal))
                .isInstanceOf(UnauthorizedException.class);
    }
}
