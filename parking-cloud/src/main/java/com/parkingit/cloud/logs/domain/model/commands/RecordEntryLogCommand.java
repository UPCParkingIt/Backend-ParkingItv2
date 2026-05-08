package com.parkingit.cloud.logs.domain.model.commands;

import java.util.UUID;

public record RecordEntryLogCommand(
        String licensePlate,
        String facialEmbedding,
        UUID parkingId,
        UUID userId
) {
}
