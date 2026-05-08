package com.parkingit.shared.domain.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class FacialSimilarityScore {
    private final Double score;  // 0.0 a 1.0
    private final Boolean isMatch;  // true si > threshold (0.6)
    private final String confidence;  // "HIGH", "MEDIUM", "LOW"

    public FacialSimilarityScore() {
        this.score = 0.0;
        this.isMatch = false;
        this.confidence = "LOW";
    }

    public static FacialSimilarityScore create(Double score) {
        if (score == null || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("Score must be between 0.0 and 1.0");
        }

        boolean isMatch = score >= 0.6;  // Threshold para "match"
        String confidence;

        if (score >= 0.85) confidence = "HIGH";
        else if (score >= 0.7) confidence = "MEDIUM";
        else confidence = "LOW";

        return new FacialSimilarityScore(score, isMatch, confidence);
    }

    private FacialSimilarityScore(Double score, Boolean isMatch, String confidence) {
        this.score = score;
        this.isMatch = isMatch;
        this.confidence = confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacialSimilarityScore that = (FacialSimilarityScore) o;
        return Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score);
    }
}