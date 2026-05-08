package com.parkingit.cloud.parking.interfaces.rest.resources;

import com.parkingit.cloud.parking.domain.model.valueobjects.AlertSeverity;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertStatus;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertResource(
        UUID id,
        UUID parkingId,
        AlertType alertType,
        AlertSeverity severity,
        String description,
        AlertStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        LocalDateTime resolvedAt,
        String reviewerNotes
) {
}
