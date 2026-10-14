package com.example.plagiarismchecker.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TfIdfCalculatorTest {

    private final TfIdfCalculator calculator = new TfIdfCalculator();

    @Test
    void computesTermFrequencyCorrectly() {
        List<String> tokens = List.of("java", "java", "language");
        Map<String, Double> tf = calculator.computeTf(tokens);

        // "java" appears 2 out of 3 total terms, "language" 1 out of 3
        assertThat(tf.get("java")).isCloseTo(2.0 / 3.0, within(0.0001));
        assertThat(tf.get("language")).isCloseTo(1.0 / 3.0, within(0.0001));
    }

    @Test
    void termAppearingInEveryDocumentGetsLowerIdfThanRareTerm() {
        List<List<String>> corpus = List.of(
                List.of("java", "language", "object"),
                List.of("java", "language", "class"),
                List.of("java", "language", "variable")
        );

        Map<String, Double> idf = calculator.computeIdf(corpus);

        // "java" and "language" appear in all 3 documents -> lower idf.
        // "object" appears in only 1 document -> higher idf.
        assertThat(idf.get("object")).isGreaterThan(idf.get("java"));
        assertThat(idf.get("object")).isGreaterThan(idf.get("language"));
    }

    @Test
    void tfIdfVectorsHaveOneEntryPerDocumentInOrder() {
        List<List<String>> corpus = List.of(
                List.of("java", "object"),
                List.of("python", "dynamic")
        );

        List<Map<String, Double>> vectors = calculator.computeTfIdfVectors(corpus);

        assertThat(vectors).hasSize(2);
        assertThat(vectors.get(0)).containsKeys("java", "object");
        assertThat(vectors.get(1)).containsKeys("python", "dynamic");
        // vector 0 should not contain terms unique to document 1
        assertThat(vectors.get(0)).doesNotContainKey("python");
    }

    @Test
    void emptyCorpusProducesNoIdfEntries() {
        assertThat(calculator.computeIdf(List.of())).isEmpty();
    }
}
