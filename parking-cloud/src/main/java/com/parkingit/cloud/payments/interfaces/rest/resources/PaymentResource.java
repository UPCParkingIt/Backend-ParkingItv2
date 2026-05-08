package com.parkingit.cloud.payments.interfaces.rest.resources;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResource(
        UUID id,
        UUID reservationId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        String referenceNumber,
        String externalTransactionId,
        LocalDateTime initiatedAt,
        LocalDateTime completedAt,
        LocalDateTime refundedAt,
        String failureReason,
        Integer retryCount
) {
}
