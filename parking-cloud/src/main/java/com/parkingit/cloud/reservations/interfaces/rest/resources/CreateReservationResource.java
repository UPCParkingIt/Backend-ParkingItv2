package com.parkingit.cloud.reservations.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReservationResource(
        UUID userId,
        UUID parkingId,
        LocalDateTime reservedFromTime
) {
}
