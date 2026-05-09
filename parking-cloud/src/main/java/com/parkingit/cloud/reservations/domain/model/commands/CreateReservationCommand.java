package com.parkingit.cloud.reservations.domain.model.commands;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReservationCommand(
        UUID userId,
        UUID parkingId,
        LocalDateTime reservedFromTime
) {
}
