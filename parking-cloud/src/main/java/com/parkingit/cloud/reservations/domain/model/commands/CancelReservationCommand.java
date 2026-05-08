package com.parkingit.cloud.reservations.domain.model.commands;

import java.util.UUID;

public record CancelReservationCommand(
        UUID reservationId,
        String reason
) {
}
