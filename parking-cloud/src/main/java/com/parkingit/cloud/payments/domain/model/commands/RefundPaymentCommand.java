package com.parkingit.cloud.payments.domain.model.commands;

import java.util.UUID;

public record RefundPaymentCommand(
        UUID paymentId,
        String reason
) {}
