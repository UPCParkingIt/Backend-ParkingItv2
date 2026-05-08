package com.parkingit.shared.domain.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class LicensePlate {
    @NotBlank(message = "License plate cannot be empty")
    private String value;

    protected LicensePlate() {}

    public LicensePlate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be empty");
        }
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException(
                    "Invalid license plate format. Expected: ABC-1234 or ABCD1234"
            );
        }
        this.value = value.toUpperCase().trim();
    }

    private static boolean isValidFormat(String plate) {
        // Peruvian format: ABC-1234 o similar
        return plate.toUpperCase().matches("^[A-Z]{3}-?\\d{3,4}$");
    }

    public boolean matches(LicensePlate other) {
        return this.value.equals(other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LicensePlate that = (LicensePlate) o;
        return Objects.equals(value, that.value);
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