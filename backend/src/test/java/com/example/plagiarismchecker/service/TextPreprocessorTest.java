package com.example.plagiarismchecker.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextPreprocessorTest {

    private final TextPreprocessor preprocessor = new TextPreprocessor();

    @Test
    void lowercasesText() {
        assertThat(preprocessor.toLowerCase("Java IS Great")).isEqualTo("java is great");
    }

    @Test
    void removesPunctuation() {
        String result = preprocessor.removePunctuation("java, is great! isn't it?");
        assertThat(result).doesNotContain(",", "!", "'", "?");
    }

    @Test
    void normalizesWhitespace() {
        assertThat(preprocessor.normalizeWhitespace("  java   is   great  ")).isEqualTo("java is great");
    }

    @Test
    void tokenizesOnWhitespace() {
        assertThat(preprocessor.tokenize("java is great")).containsExactly("java", "is", "great");
    }

    @Test
    void removesStopWords() {
        List<String> tokens = List.of("java", "is", "an", "object", "oriented", "the", "language");
        List<String> filtered = preprocessor.removeStopWords(tokens);
        assertThat(filtered).containsExactly("java", "object", "oriented", "language");
    }

    @Test
    void fullPipelineProducesCleanTokens() {
        List<String> tokens = preprocessor.preprocess("Java is an Object-Oriented programming language!");
        assertThat(tokens).contains("java", "object", "oriented", "programming", "language");
        assertThat(tokens).doesNotContain("is", "an");
    }

    @Test
    void emptyTextProducesNoTokens() {
        assertThat(preprocessor.preprocess("")).isEmpty();
        assertThat(preprocessor.preprocess(null)).isEmpty();
    }
}
