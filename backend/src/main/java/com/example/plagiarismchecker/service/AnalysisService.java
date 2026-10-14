package com.example.plagiarismchecker.service;

import com.example.plagiarismchecker.dto.ComparisonResponse;
import com.example.plagiarismchecker.dto.DashboardStatsResponse;
import com.example.plagiarismchecker.dto.SimilarityResultResponse;
import com.example.plagiarismchecker.entity.Assignment;
import com.example.plagiarismchecker.entity.SimilarityLevel;
import com.example.plagiarismchecker.entity.SimilarityResult;
import com.example.plagiarismchecker.entity.Submission;
import com.example.plagiarismchecker.exception.BadRequestException;
import com.example.plagiarismchecker.exception.ResourceNotFoundException;
import com.example.plagiarismchecker.repository.AssignmentRepository;
import com.example.plagiarismchecker.repository.SimilarityResultRepository;
import com.example.plagiarismchecker.repository.SubmissionRepository;
import com.example.plagiarismchecker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orchestrates the full end-to-end similarity analysis workflow for one
 * assignment:
 *
 *   1. Load every submission for the assignment.
 *   2. Preprocess each submission's text (TextPreprocessor).
 *   3. Build TF-IDF vectors for the whole submission set (TfIdfCalculator).
 *   4. Compute cosine similarity for every distinct pair (CosineSimilarityCalculator).
 *   5. Classify each pair as LOW / MODERATE / HIGH and flag pairs that need review.
 *   6. Persist the results to MySQL, replacing any previous run for this assignment.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SimilarityResultRepository similarityResultRepository;
    private final AssignmentService assignmentService;

    private final TextPreprocessor textPreprocessor;
    private final TfIdfCalculator tfIdfCalculator;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    @Value("${app.similarity.threshold.moderate}")
    private double moderateThreshold;

    @Value("${app.similarity.threshold.high}")
    private double highThreshold;

    @Transactional
    public List<SimilarityResultResponse> analyze(Long assignmentId, UserPrincipal currentUser) {
        Assignment assignment = assignmentService.findAssignmentOrThrow(assignmentId);
        assignmentService.requireOwnership(assignment, currentUser);

        List<Submission> submissions = submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(assignmentId);

        if (submissions.size() < 2) {
            throw new BadRequestException("At least 2 submissions are required to run a similarity analysis");
        }

        // Step 1 & 2: preprocess every submission into a token list. The
        // ordering of `corpus` matches the ordering of `submissions`.
        List<List<String>> corpus = submissions.stream()
                .map(s -> textPreprocessor.preprocess(s.getContent()))
                .toList();

        // Step 3: TF-IDF vectors for the whole assignment's submission set.
        List<Map<String, Double>> vectors = tfIdfCalculator.computeTfIdfVectors(corpus);

        // Wipe any previous analysis run for this assignment so re-running
        // "Analyze Similarity" always reflects the current submissions.
        similarityResultRepository.deleteByAssignmentId(assignmentId);

        List<SimilarityResult> results = new ArrayList<>();

        // Step 4 & 5: pairwise cosine similarity + classification.
        // Every submission is compared with every other submission exactly
        // once (A-B, A-C, A-D, B-C, ...), never with itself.
        for (int i = 0; i < submissions.size(); i++) {
            for (int j = i + 1; j < submissions.size(); j++) {
                double percentage = cosineSimilarityCalculator.cosineSimilarityAsPercentage(vectors.get(i), vectors.get(j));
                SimilarityLevel level = classify(percentage);
                boolean flagged = percentage >= highThreshold;

                SimilarityResult result = SimilarityResult.builder()
                        .assignment(assignment)
                        .submission1(submissions.get(i))
                        .submission2(submissions.get(j))
                        .similarityPercentage(percentage)
                        .similarityLevel(level)
                        .flagged(flagged)
                        .build();

                results.add(result);
            }
        }

        // Step 6: persist.
        List<SimilarityResult> saved = similarityResultRepository.saveAll(results);

        return saved.stream().map(this::toResponse).toList();
    }

    public List<SimilarityResultResponse> getResults(Long assignmentId, UserPrincipal currentUser) {
        Assignment assignment = assignmentService.findAssignmentOrThrow(assignmentId);
        assignmentService.requireOwnership(assignment, currentUser);

        return similarityResultRepository.findByAssignmentIdOrderBySimilarityPercentageDesc(assignmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public SimilarityResultResponse getById(Long resultId, UserPrincipal currentUser) {
        SimilarityResult result = findResultOrThrow(resultId);
        assignmentService.requireOwnership(result.getAssignment(), currentUser);
        return toResponse(result);
    }

    public ComparisonResponse getComparison(Long resultId, UserPrincipal currentUser) {
        SimilarityResult result = findResultOrThrow(resultId);
        assignmentService.requireOwnership(result.getAssignment(), currentUser);

        Submission sub1 = result.getSubmission1();
        Submission sub2 = result.getSubmission2();

        List<String> tokens1 = textPreprocessor.preprocess(sub1.getContent());
        List<String> tokens2 = textPreprocessor.preprocess(sub2.getContent());

        Set<String> common = new HashSet<>(tokens1);
        common.retainAll(new HashSet<>(tokens2));

        return ComparisonResponse.builder()
                .resultId(result.getId())
                .assignmentId(result.getAssignment().getId())
                .assignmentTitle(result.getAssignment().getTitle())
                .submissionId1(sub1.getId())
                .student1Name(sub1.getStudent().getName())
                .content1(sub1.getContent())
                .submissionId2(sub2.getId())
                .student2Name(sub2.getStudent().getName())
                .content2(sub2.getContent())
                .similarityPercentage(result.getSimilarityPercentage())
                .similarityLevel(result.getSimilarityLevel())
                .flagged(result.isFlagged())
                .commonSignificantTerms(common.stream().sorted().toList())
                .disclaimer("A high similarity score is a screening signal, not proof of plagiarism. "
                        + "Paraphrasing can lower this score and common phrasing can raise it - "
                        + "please review both submissions yourself before making any academic decision.")
                .build();
    }

    public DashboardStatsResponse getDashboardStats(UserPrincipal currentUser) {
        List<Assignment> assignments = assignmentRepository.findByTeacherIdOrderByCreatedAtDesc(currentUser.getId());
        List<Long> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        long totalSubmissions = assignmentIds.isEmpty() ? 0 : submissionRepository.countByAssignmentIdIn(assignmentIds);
        long flaggedPairs = assignmentIds.isEmpty() ? 0 : similarityResultRepository.countByAssignmentIdInAndFlaggedTrue(assignmentIds);

        double highest = assignmentIds.stream()
                .flatMap(id -> similarityResultRepository.findByAssignmentIdOrderBySimilarityPercentageDesc(id).stream())
                .mapToDouble(SimilarityResult::getSimilarityPercentage)
                .max()
                .orElse(0.0);

        return DashboardStatsResponse.builder()
                .totalAssignments(assignments.size())
                .totalSubmissions(totalSubmissions)
                .flaggedPairs(flaggedPairs)
                .highestSimilarity(highest)
                .build();
    }

    /**
     * Applies the configurable LOW / MODERATE / HIGH thresholds. Kept as its
     * own method so it is trivial to unit test in isolation.
     */
    SimilarityLevel classify(double percentage) {
        if (percentage >= highThreshold) {
            return SimilarityLevel.HIGH;
        } else if (percentage >= moderateThreshold) {
            return SimilarityLevel.MODERATE;
        }
        return SimilarityLevel.LOW;
    }

    private SimilarityResult findResultOrThrow(Long id) {
        return similarityResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Similarity result not found with id: " + id));
    }

    private SimilarityResultResponse toResponse(SimilarityResult result) {
        return SimilarityResultResponse.builder()
                .id(result.getId())
                .assignmentId(result.getAssignment().getId())
                .submissionId1(result.getSubmission1().getId())
                .student1Name(result.getSubmission1().getStudent().getName())
                .submissionId2(result.getSubmission2().getId())
                .student2Name(result.getSubmission2().getStudent().getName())
                .similarityPercentage(result.getSimilarityPercentage())
                .similarityLevel(result.getSimilarityLevel())
                .flagged(result.isFlagged())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
