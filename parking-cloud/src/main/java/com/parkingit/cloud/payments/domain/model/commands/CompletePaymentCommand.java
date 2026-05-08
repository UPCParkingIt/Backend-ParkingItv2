package com.parkingit.cloud.payments.domain.model.commands;

import java.util.UUID;

public record CompletePaymentCommand(
        UUID paymentId,
        String externalTransactionId
) {}
