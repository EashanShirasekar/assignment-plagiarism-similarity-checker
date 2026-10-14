package com.example.plagiarismchecker.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A from-scratch implementation of TF-IDF (Term Frequency - Inverse Document
 * Frequency). No external NLP/ML library is used, so the whole calculation
 * can be explained line by line during a viva.
 *
 * TF  (term frequency):   how often a word appears in ONE document,
 *                         normalized by the document length.
 *                         tf(t, d) = count(t in d) / totalTerms(d)
 *
 * IDF (inverse document   how rare/important a word is across ALL
 *      frequency):        documents in the collection (the corpus).
 *                         A smoothed formula is used so that words which
 *                         appear in every document still get a small,
 *                         non-zero weight instead of exactly zero:
 *                         idf(t) = ln((1 + N) / (1 + df(t))) + 1
 *                         where N = number of documents,
 *                               df(t) = number of documents containing t
 *
 * TF-IDF: tfidf(t, d) = tf(t, d) * idf(t)
 *
 * Each document ends up represented as a Map<String, Double> - a sparse
 * vector where the key is the term and the value is its TF-IDF weight.
 */
@Component
public class TfIdfCalculator {

    /**
     * Term frequency for a single document.
     */
    public Map<String, Double> computeTf(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        if (tokens.isEmpty()) {
            return tf;
        }

        Map<String, Integer> rawCounts = new HashMap<>();
        for (String token : tokens) {
            rawCounts.merge(token, 1, Integer::sum);
        }

        int totalTerms = tokens.size();
        for (Map.Entry<String, Integer> entry : rawCounts.entrySet()) {
            tf.put(entry.getKey(), entry.getValue() / (double) totalTerms);
        }
        return tf;
    }

    /**
     * Inverse document frequency for every distinct term across the whole
     * corpus (all submissions belonging to one assignment).
     */
    public Map<String, Double> computeIdf(List<List<String>> corpus) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocuments = corpus.size();
        if (totalDocuments == 0) {
            return idf;
        }

        // document frequency: how many documents contain each term at least once
        Map<String, Integer> documentFrequency = new HashMap<>();
        for (List<String> document : corpus) {
            Set<String> distinctTermsInDoc = new HashSet<>(document);
            for (String term : distinctTermsInDoc) {
                documentFrequency.merge(term, 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
            double smoothedIdf = Math.log((1.0 + totalDocuments) / (1.0 + entry.getValue())) + 1.0;
            idf.put(entry.getKey(), smoothedIdf);
        }
        return idf;
    }

    /**
     * Builds one TF-IDF vector per document in the corpus.
     * The returned list is in the same order as the input corpus.
     */
    public List<Map<String, Double>> computeTfIdfVectors(List<List<String>> corpus) {
        Map<String, Double> idf = computeIdf(corpus);

        return corpus.stream()
                .map(document -> {
                    Map<String, Double> tf = computeTf(document);
                    Map<String, Double> tfidf = new HashMap<>();
                    for (Map.Entry<String, Double> entry : tf.entrySet()) {
                        String term = entry.getKey();
                        double tfValue = entry.getValue();
                        double idfValue = idf.getOrDefault(term, 0.0);
                        tfidf.put(term, tfValue * idfValue);
                    }
                    return tfidf;
                })
                .toList();
    }
}
