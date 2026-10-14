package com.example.plagiarismchecker.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CosineSimilarityCalculatorTest {

    private final CosineSimilarityCalculator calculator = new CosineSimilarityCalculator();

    @Test
    void identicalVectorsHaveSimilarityOfOne() {
        Map<String, Double> vector = Map.of("java", 0.5, "object", 0.3, "language", 0.2);
        double similarity = calculator.cosineSimilarity(vector, vector);
        assertThat(similarity).isCloseTo(1.0, within(0.0001));
    }

    @Test
    void completelyDisjointVectorsHaveSimilarityOfZero() {
        Map<String, Double> vectorA = Map.of("java", 0.8, "object", 0.6);
        Map<String, Double> vectorB = Map.of("python", 0.9, "dynamic", 0.4);
        double similarity = calculator.cosineSimilarity(vectorA, vectorB);
        assertThat(similarity).isEqualTo(0.0);
    }

    @Test
    void partiallyOverlappingVectorsProduceIntermediateSimilarity() {
        Map<String, Double> vectorA = Map.of("java", 0.7, "object", 0.5, "unique1", 0.3);
        Map<String, Double> vectorB = Map.of("java", 0.6, "object", 0.4, "unique2", 0.5);

        double similarity = calculator.cosineSimilarity(vectorA, vectorB);

        assertThat(similarity).isGreaterThan(0.0);
        assertThat(similarity).isLessThan(1.0);
    }

    @Test
    void emptyVectorProducesZeroSimilarity() {
        assertThat(calculator.cosineSimilarity(Map.of(), Map.of("java", 1.0))).isEqualTo(0.0);
        assertThat(calculator.cosineSimilarity(Map.of(), Map.of())).isEqualTo(0.0);
    }

    @Test
    void percentageConversionIsCorrect() {
        Map<String, Double> vector = Map.of("java", 1.0);
        double percentage = calculator.cosineSimilarityAsPercentage(vector, vector);
        assertThat(percentage).isCloseTo(100.0, within(0.01));
    }
}
