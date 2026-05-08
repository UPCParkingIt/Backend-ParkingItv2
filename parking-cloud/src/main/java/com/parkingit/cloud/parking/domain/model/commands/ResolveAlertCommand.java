package com.parkingit.cloud.parking.domain.model.commands;

import java.util.UUID;

public record ResolveAlertCommand(UUID alertId, String reviewerNotes) {
}
