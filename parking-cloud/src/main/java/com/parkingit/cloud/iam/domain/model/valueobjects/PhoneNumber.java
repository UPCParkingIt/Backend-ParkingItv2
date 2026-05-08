package com.parkingit.cloud.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class PhoneNumber {
    @Column(name = "phone_number", length = 20)
    private String value;

    protected PhoneNumber() {}

    public PhoneNumber(String value) {
        if (value != null && !value.trim().isEmpty()) {
            String normalized = value.trim();
            if (!isValidPhoneNumber(normalized)) {
                throw new IllegalArgumentException("Invalid phone number format: " + value);
            }
            this.value = normalized;
        }
    }

    private static boolean isValidPhoneNumber(String phone) {
        return phone.matches("^(\\+51|0)?9\\d{8}$");
    }

    public static PhoneNumber create(String value) {
        return new PhoneNumber(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
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
