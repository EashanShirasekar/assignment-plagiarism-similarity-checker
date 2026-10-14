package com.example.plagiarismchecker.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cleans raw submission text and turns it into a list of tokens that the
 * TF-IDF calculator can work with.
 *
 * Steps (in order):
 *   1. Lowercase everything.
 *   2. Strip punctuation / non-alphanumeric characters.
 *   3. Normalize whitespace.
 *   4. Tokenize on whitespace.
 *   5. Drop common English stop words and empty tokens.
 */
@Component
public class TextPreprocessor {

    /**
     * A small, deliberately simple stop-word list. It is not exhaustive -
     * for a college microproject the goal is to remove the most common
     * low-information words (articles, prepositions, pronouns, etc.) so
     * that the TF-IDF weights are dominated by more meaningful terms.
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "if", "then", "so", "because",
            "of", "to", "in", "on", "at", "by", "for", "with", "about", "against",
            "between", "into", "through", "during", "before", "after", "above",
            "below", "from", "up", "down", "out", "off", "over", "under", "again",
            "further", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "having", "do", "does", "did", "doing",
            "this", "that", "these", "those", "it", "its", "as", "than",
            "too", "very", "can", "will", "just", "should", "now", "i", "you",
            "he", "she", "we", "they", "them", "their", "our", "your", "his",
            "her", "not", "no", "which", "who", "whom", "what", "when",
            "where", "why", "how", "all", "any", "both", "each", "few",
            "more", "most", "other", "some", "such", "only", "own", "same",
            "there", "here", "also", "into", "used", "using", "use"
    ));

    /**
     * Runs the full preprocessing pipeline and returns the cleaned tokens.
     */
    public List<String> preprocess(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }

        String lowerCased = toLowerCase(rawText);
        String withoutPunctuation = removePunctuation(lowerCased);
        String normalized = normalizeWhitespace(withoutPunctuation);
        List<String> tokens = tokenize(normalized);
        return removeStopWords(tokens);
    }

    public String toLowerCase(String text) {
        return text.toLowerCase();
    }

    /**
     * Removes everything that isn't a letter, digit, or whitespace.
     */
    public String removePunctuation(String text) {
        return text.replaceAll("[^a-z0-9\\s]", " ");
    }

    public String normalizeWhitespace(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }

    public List<String> tokenize(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return Arrays.asList(text.split(" "));
    }

    public List<String> removeStopWords(List<String> tokens) {
        return tokens.stream()
                .filter(token -> !token.isBlank())
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toList());
    }
}
