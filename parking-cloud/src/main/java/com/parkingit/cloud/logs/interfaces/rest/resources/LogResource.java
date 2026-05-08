package com.parkingit.cloud.logs.interfaces.rest.resources;

import com.parkingit.cloud.logs.domain.model.valueobjects.LogStatus;

import java.time.Instant;
import java.util.UUID;

public record LogResource(
        UUID id,
        String licensePlate,
        UUID parkingId,
        UUID userId,
        LogStatus status,
        Long occupancyDurationMinutes,
        Boolean isAlertGenerated,
        String alertReason,
        Instant entryTimestamp,
        Instant exitTimestamp,
        Instant createdAt
) {
}
