package com.parkingit.cloud.payments.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum PaymentStage {
    PENDING_DRIVER_PAYMENT("Waiting for driver to pay"),
    PENDING_ADMIN_REVIEW("Waiting for admin approval"),
    APPROVED_BY_ADMIN("Approved and completed"),
    REJECTED_BY_ADMIN("Rejected by admin"),
    DRIVER_EXITED_ALLOWED("Driver allowed to exit"),
    DRIVER_BLOCKED_AT_EXIT("Driver blocked at exit gate");

    private final String description;

    PaymentStage(String description) {
        this.description = description;
    }
}
