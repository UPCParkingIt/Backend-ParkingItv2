package com.parkingit.cloud.payments.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Awaiting payment"),
    PROCESSING("Processing payment"),
    COMPLETED("Payment successful"),
    FAILED("Payment failed"),
    REFUNDED("Payment refunded"),
    CANCELLED("Payment cancelled"),
    EXPIRED("Payment link expired");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}
