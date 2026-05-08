package com.parkingit.cloud.logs.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordExitLogCommand(
        UUID entryLogId,
        String licensePlate,
        String facialEmbedding,
        Boolean isMatched,
        BigDecimal confidenceScore,
        UUID parkingId
) {
}
