package com.example.plagiarismchecker.service;

import com.example.plagiarismchecker.dto.SimilarityResultResponse;
import com.example.plagiarismchecker.entity.*;
import com.example.plagiarismchecker.exception.BadRequestException;
import com.example.plagiarismchecker.repository.AssignmentRepository;
import com.example.plagiarismchecker.repository.SimilarityResultRepository;
import com.example.plagiarismchecker.repository.SubmissionRepository;
import com.example.plagiarismchecker.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Exercises the full analyze() workflow (preprocessing -> TF-IDF -> cosine
 * similarity -> classification -> persistence) using the REAL algorithm
 * classes, with only the repositories mocked. This is the closest thing to
 * an end-to-end test of the similarity engine without needing a database.
 */
@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private SimilarityResultRepository similarityResultRepository;
    @Mock
    private AssignmentService assignmentService;

    private AnalysisService analysisService;

    private User teacher;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(
                submissionRepository,
                assignmentRepository,
                similarityResultRepository,
                assignmentService,
                new TextPreprocessor(),
                new TfIdfCalculator(),
                new CosineSimilarityCalculator()
        );
        ReflectionTestUtils.setField(analysisService, "moderateThreshold", 40.0);
        ReflectionTestUtils.setField(analysisService, "highThreshold", 70.0);

        teacher = User.builder().id(1L).name("Dr. Mehta").email("t@college.edu").password("hash").role(Role.TEACHER).build();
        assignment = Assignment.builder().id(10L).title("OOP").description("desc")
                .deadline(LocalDateTime.now().plusDays(3)).teacher(teacher).build();
    }

    private Submission submission(long id, User student, String content) {
        return Submission.builder().id(id).assignment(assignment).student(student).content(content).build();
    }

    private User student(long id, String name) {
        return User.builder().id(id).name(name).email(name + "@college.edu").password("hash").role(Role.STUDENT).build();
    }

    @Test
    void identicalSubmissionsScoreApproximatelyOneHundredPercent() {
        User s1 = student(2L, "Rahul");
        User s2 = student(3L, "Amit");
        String text = "Java is an object oriented programming language used to build applications.";

        List<Submission> submissions = List.of(submission(100L, s1, text), submission(101L, s2, text));

        UserPrincipal teacherPrincipal = new UserPrincipal(teacher);
        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(10L)).thenReturn(submissions);
        when(similarityResultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SimilarityResultResponse> results = analysisService.analyze(10L, teacherPrincipal);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSimilarityPercentage()).isGreaterThan(95.0);
        assertThat(results.get(0).getSimilarityLevel()).isEqualTo(SimilarityLevel.HIGH);
        assertThat(results.get(0).isFlagged()).isTrue();
    }

    @Test
    void completelyDifferentSubmissionsScoreLow() {
        User s1 = student(2L, "Rahul");
        User s2 = student(3L, "Amit");

        List<Submission> submissions = List.of(
                submission(100L, s1, "Java is an object oriented programming language used to build applications."),
                submission(101L, s2, "The Amazon rainforest produces a large share of the world's oxygen supply.")
        );

        UserPrincipal teacherPrincipal = new UserPrincipal(teacher);
        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(10L)).thenReturn(submissions);
        when(similarityResultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SimilarityResultResponse> results = analysisService.analyze(10L, teacherPrincipal);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSimilarityPercentage()).isLessThan(40.0);
        assertThat(results.get(0).getSimilarityLevel()).isEqualTo(SimilarityLevel.LOW);
        assertThat(results.get(0).isFlagged()).isFalse();
    }

    @Test
    void paraphrasedSubmissionsScoreInModerateToHighRange() {
        User s1 = student(2L, "Rahul");
        User s2 = student(3L, "Amit");

        List<Submission> submissions = List.of(
                submission(100L, s1, "Java is an object oriented programming language used to develop applications."),
                submission(101L, s2, "Java is an object oriented language commonly used for developing applications.")
        );

        UserPrincipal teacherPrincipal = new UserPrincipal(teacher);
        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(10L)).thenReturn(submissions);
        when(similarityResultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SimilarityResultResponse> results = analysisService.analyze(10L, teacherPrincipal);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSimilarityPercentage()).isGreaterThan(40.0);
    }

    @Test
    void fourSubmissionsProduceSixPairwiseComparisonsNeverComparingASubmissionWithItself() {
        User s1 = student(2L, "A");
        User s2 = student(3L, "B");
        User s3 = student(4L, "C");
        User s4 = student(5L, "D");

        List<Submission> submissions = List.of(
                submission(100L, s1, "Java is a programming language."),
                submission(101L, s2, "Python is a programming language."),
                submission(102L, s3, "The stock market fell sharply today."),
                submission(103L, s4, "Cats and dogs are common household pets.")
        );

        UserPrincipal teacherPrincipal = new UserPrincipal(teacher);
        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(10L)).thenReturn(submissions);
        when(similarityResultRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SimilarityResultResponse> results = analysisService.analyze(10L, teacherPrincipal);

        // C(4,2) = 6 pairs, and no pair should compare a submission with itself
        assertThat(results).hasSize(6);
        results.forEach(r -> assertThat(r.getSubmissionId1()).isNotEqualTo(r.getSubmissionId2()));
    }

    @Test
    void analyzeRequiresAtLeastTwoSubmissions() {
        User s1 = student(2L, "Rahul");
        List<Submission> submissions = List.of(submission(100L, s1, "Only one submission."));

        UserPrincipal teacherPrincipal = new UserPrincipal(teacher);
        when(assignmentService.findAssignmentOrThrow(10L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(10L)).thenReturn(submissions);

        assertThatThrownBy(() -> analysisService.analyze(10L, teacherPrincipal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void classifyAppliesThresholdsCorrectly() {
        assertThat(analysisService.classify(20.0)).isEqualTo(SimilarityLevel.LOW);
        assertThat(analysisService.classify(39.99)).isEqualTo(SimilarityLevel.LOW);
        assertThat(analysisService.classify(40.0)).isEqualTo(SimilarityLevel.MODERATE);
        assertThat(analysisService.classify(69.99)).isEqualTo(SimilarityLevel.MODERATE);
        assertThat(analysisService.classify(70.0)).isEqualTo(SimilarityLevel.HIGH);
        assertThat(analysisService.classify(95.0)).isEqualTo(SimilarityLevel.HIGH);
    }
}
