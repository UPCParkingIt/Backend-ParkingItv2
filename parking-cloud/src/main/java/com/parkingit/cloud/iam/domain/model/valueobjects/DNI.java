package com.parkingit.cloud.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class DNI {
    @Column(name = "dni_number", unique = true, length = 8)
    private String value;

    protected DNI() {}

    public DNI(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DNI cannot be empty");
        }

        String normalized = value.trim();
        if (!isValidDNI(normalized)) {
            throw new IllegalArgumentException("Invalid DNI format. Expected 8 digits: " + value);
        }

        this.value = normalized;
    }

    private static boolean isValidDNI(String dni) {
        return dni.matches("^\\d{8}$");
    }

    public static DNI create(String value) {
        return new DNI(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DNI dni = (DNI) o;
        return Objects.equals(value, dni.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
