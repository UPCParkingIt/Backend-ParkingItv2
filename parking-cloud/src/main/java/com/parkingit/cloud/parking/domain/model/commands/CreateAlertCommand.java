package com.parkingit.cloud.parking.domain.model.commands;

import com.parkingit.cloud.parking.domain.model.valueobjects.AlertSeverity;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;

import java.util.UUID;

public record CreateAlertCommand(
        UUID parkingId,
        AlertType alertType,
        AlertSeverity severity,
        String description,
        UUID parkingLogId
) {
}
