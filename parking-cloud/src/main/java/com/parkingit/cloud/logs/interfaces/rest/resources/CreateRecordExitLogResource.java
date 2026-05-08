package com.parkingit.cloud.logs.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRecordExitLogResource(
        UUID entryLogId,
        String licensePlate,
        String facialEmbedding,
        Boolean isMatched,
        BigDecimal confidenceScore,
        UUID parkingId
) {
}
