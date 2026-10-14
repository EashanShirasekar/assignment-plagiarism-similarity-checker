package com.example.plagiarismchecker.service;

import com.example.plagiarismchecker.dto.AssignmentRequest;
import com.example.plagiarismchecker.dto.AssignmentResponse;
import com.example.plagiarismchecker.entity.Assignment;
import com.example.plagiarismchecker.entity.Role;
import com.example.plagiarismchecker.entity.User;
import com.example.plagiarismchecker.exception.UnauthorizedException;
import com.example.plagiarismchecker.repository.AssignmentRepository;
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
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private User teacherEntity;
    private UserPrincipal teacherPrincipal;
    private UserPrincipal studentPrincipal;

    @BeforeEach
    void setUp() {
        teacherEntity = User.builder().id(1L).name("Dr. Mehta").email("teacher1@college.edu").password("hash").role(Role.TEACHER).build();
        User studentEntity = User.builder().id(2L).name("Rahul").email("student1@college.edu").password("hash").role(Role.STUDENT).build();

        teacherPrincipal = new UserPrincipal(teacherEntity);
        studentPrincipal = new UserPrincipal(studentEntity);
    }

    @Test
    void teacherCanCreateAssignment() {
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("OOP Basics");
        request.setDescription("Explain OOP in Java");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacherEntity));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> {
            Assignment a = invocation.getArgument(0);
            a.setId(100L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });
        when(submissionRepository.countByAssignmentId(100L)).thenReturn(0L);

        AssignmentResponse response = assignmentService.create(request, teacherPrincipal);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTitle()).isEqualTo("OOP Basics");
        assertThat(response.getTeacherId()).isEqualTo(1L);
    }

    @Test
    void studentCannotCreateAssignment() {
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("OOP Basics");
        request.setDescription("Explain OOP in Java");
        request.setDeadline(LocalDateTime.now().plusDays(7));

        assertThatThrownBy(() -> assignmentService.create(request, studentPrincipal))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void onlyOwningTeacherCanDeleteAssignment() {
        User otherTeacherEntity = User.builder().id(9L).name("Prof. Other").email("teacher2@college.edu").password("hash").role(Role.TEACHER).build();
        UserPrincipal otherTeacherPrincipal = new UserPrincipal(otherTeacherEntity);

        Assignment assignment = Assignment.builder().id(5L).title("X").description("Y")
                .deadline(LocalDateTime.now().plusDays(1)).teacher(teacherEntity).build();

        when(assignmentRepository.findById(5L)).thenReturn(Optional.of(assignment));

        assertThatThrownBy(() -> assignmentService.delete(5L, otherTeacherPrincipal))
                .isInstanceOf(UnauthorizedException.class);
    }
}
