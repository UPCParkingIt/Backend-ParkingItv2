package com.parkingit.cloud.payments.domain.model.commands;

import java.util.UUID;

public record ApprovePaymentCommand(
        UUID paymentId,
        UUID adminUserId,
        String notes
) {
}
