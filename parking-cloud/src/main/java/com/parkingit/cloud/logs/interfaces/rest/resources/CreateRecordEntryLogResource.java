package com.parkingit.cloud.logs.interfaces.rest.resources;

import java.util.UUID;

public record CreateRecordEntryLogResource(
        String licensePlate,
        String facialEmbedding,
        UUID parkingId,
        UUID userId
) {
}
