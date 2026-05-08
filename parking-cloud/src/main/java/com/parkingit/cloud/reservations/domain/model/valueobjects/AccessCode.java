package com.parkingit.cloud.reservations.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccessCode {
    private String code;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private Boolean used;

    /**
     * Generates an access code that expires at the given instant.
     * For new reservations, expiresAt = reservedFromTime + 15 min (grace period).
     */
    public static AccessCode generate(LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new AccessCode(code, now, expiresAt, false);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markAsUsed() {
        if (!isValid()) {
            throw new IllegalStateException("Access code is not valid or has already expired");
        }
        this.used = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessCode that = (AccessCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
