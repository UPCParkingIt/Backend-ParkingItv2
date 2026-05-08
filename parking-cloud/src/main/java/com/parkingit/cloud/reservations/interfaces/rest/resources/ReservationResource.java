package com.parkingit.cloud.reservations.interfaces.rest.resources;

import com.parkingit.cloud.reservations.domain.model.valueobjects.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public record ReservationResource(
        UUID id,
        UUID userId,
        UUID parkingId,
        LocalDateTime reservedFromTime,
        LocalDateTime accessCodeExpiresAt,
        String accessCode,
        ReservationStatus status,
        BigDecimal reservationFee,
        LocalDateTime entryTime,
        Date createdAt
) {
}
