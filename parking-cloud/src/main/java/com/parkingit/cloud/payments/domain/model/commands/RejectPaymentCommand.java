package com.parkingit.cloud.payments.domain.model.commands;

import java.util.UUID;

public record RejectPaymentCommand(
        UUID paymentId,
        UUID adminUserId,
        String reason
) {
}
