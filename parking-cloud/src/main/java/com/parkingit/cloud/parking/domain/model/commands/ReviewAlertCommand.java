package com.parkingit.cloud.parking.domain.model.commands;

import java.util.UUID;

public record ReviewAlertCommand(UUID alertId, String reviewerNotes) {
}
