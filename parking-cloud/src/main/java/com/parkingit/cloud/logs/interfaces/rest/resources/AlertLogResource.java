package com.parkingit.cloud.logs.interfaces.rest.resources;

import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;

import java.time.Instant;
import java.util.UUID;

public record AlertLogResource(
        UUID id,
        String licensePlate,
        UUID parkingId,
        String alertReason,
        AlertType alertType,
        Instant alertTimestamp
) {
}
