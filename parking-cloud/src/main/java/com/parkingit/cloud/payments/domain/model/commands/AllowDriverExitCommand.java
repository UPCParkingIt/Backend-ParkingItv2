package com.parkingit.cloud.payments.domain.model.commands;

import java.util.UUID;

public record AllowDriverExitCommand(
        UUID paymentId
) {
}
