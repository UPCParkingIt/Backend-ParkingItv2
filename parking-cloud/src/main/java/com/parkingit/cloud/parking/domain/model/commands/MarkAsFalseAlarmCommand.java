package com.parkingit.cloud.parking.domain.model.commands;

import java.util.UUID;

public record MarkAsFalseAlarmCommand(UUID alertId, String reviewerNotes) {
}
