package com.parkingit.cloud.parking.domain.model.commands;

import java.util.UUID;

public record OccupySpotCommand(
        UUID parkingId,
        String licensePlate
) {
}
