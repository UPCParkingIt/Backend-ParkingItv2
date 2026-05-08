package com.parkingit.cloud.payments.interfaces.rest.resources;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record InitiatePaymentResource(
        UUID reservationId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String description
) {
}
