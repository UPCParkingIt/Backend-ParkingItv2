package com.parkingit.cloud.payments.domain.model.commands;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record InitiatePaymentCommand(
        UUID reservationId,
        UUID parkingLogId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String description
) {}
