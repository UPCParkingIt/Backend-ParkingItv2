package com.parkingit.cloud.parking.domain.model.commands;

import java.util.UUID;

public record ReleaseSpotCommand(
        UUID parkingId,
        String licensePlate
) {
}
