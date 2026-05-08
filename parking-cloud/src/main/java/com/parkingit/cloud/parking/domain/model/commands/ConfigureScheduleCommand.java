package com.parkingit.cloud.parking.domain.model.commands;

import java.time.LocalTime;
import java.util.UUID;

public record ConfigureScheduleCommand(
        UUID parkingId,
        LocalTime openTime,
        LocalTime closeTime,
        String businessDays
) {
}
