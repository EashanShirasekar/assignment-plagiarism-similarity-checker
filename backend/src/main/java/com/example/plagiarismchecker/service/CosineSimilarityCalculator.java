package com.example.plagiarismchecker.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes cosine similarity between two sparse TF-IDF vectors.
 *
 *   cosine similarity = (A . B) / (||A|| * ||B||)
 *
 * The result is always between 0 (completely dissimilar) and 1 (identical
 * direction / wording emphasis). It is converted to a 0-100 percentage by
 * the caller.
 */
@Component
public class CosineSimilarityCalculator {

    public double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA.isEmpty() || vectorB.isEmpty()) {
            return 0.0;
        }

        double dotProduct = dotProduct(vectorA, vectorB);
        double magnitudeA = magnitude(vectorA);
        double magnitudeB = magnitude(vectorB);

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (magnitudeA * magnitudeB);

        // Guard against tiny floating point drift pushing us just above 1.0
        // or just below 0.0.
        return Math.max(0.0, Math.min(1.0, similarity));
    }

    /**
     * Convenience method: returns the similarity already converted to a
     * percentage rounded to 2 decimal places, e.g. 0.8734 -> 87.34
     */
    public double cosineSimilarityAsPercentage(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        double similarity = cosineSimilarity(vectorA, vectorB);
        return Math.round(similarity * 10000.0) / 100.0;
    }

    private double dotProduct(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        double sum = 0.0;
        // Iterate the smaller map for a small efficiency win; correctness
        // does not depend on which map we iterate.
        Map<String, Double> smaller = vectorA.size() <= vectorB.size() ? vectorA : vectorB;
        Map<String, Double> larger = smaller == vectorA ? vectorB : vectorA;

        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            Double otherValue = larger.get(entry.getKey());
            if (otherValue != null) {
                sum += entry.getValue() * otherValue;
            }
        }
        return sum;
    }

    private double magnitude(Map<String, Double> vector) {
        double sumOfSquares = 0.0;
        for (double value : vector.values()) {
            sumOfSquares += value * value;
        }
        return Math.sqrt(sumOfSquares);
    }

    /**
     * Not used in the similarity math itself, but handy for debugging /
     * explaining how many terms two documents actually share.
     */
    public int sharedTermCount(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        Set<String> shared = new HashSet<>(vectorA.keySet());
        shared.retainAll(vectorB.keySet());
        return shared.size();
    }
}
