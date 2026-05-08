package com.parkingit.cloud.logs.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import java.math.BigDecimal;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Embeddable
public class VerificationResult {
    Boolean isMatched;
    BigDecimal confidenceScore;

    public static VerificationResult create(Boolean isMatched, BigDecimal confidenceScore) {
        if (confidenceScore == null || confidenceScore.compareTo(BigDecimal.ZERO) < 0 ||
                confidenceScore.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Confidence score must be between 0.0 and 1.0");
        }
        return new VerificationResult(isMatched, confidenceScore);
    }

    public boolean isHighConfidence() {
        assert confidenceScore != null;
        return confidenceScore.compareTo(new BigDecimal("0.85")) >= 0;
    }
}
