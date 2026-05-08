package com.parkingit.cloud.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class HashedPassword {
    @Column(name = "password_hash", nullable = false, length = 255)
    private String hash;

    protected HashedPassword() {}

    public HashedPassword(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        this.hash = hash;
    }

    public static HashedPassword fromHash(String hash) {
        return new HashedPassword(hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashedPassword that = (HashedPassword) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return "HashedPassword[***]";
    }
}
