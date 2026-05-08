package com.parkingit.cloud.parking.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateParkingCommand(
        UUID id,
        String parkingName,
        Double latitude,
        Double longitude,
        String address,
        LocalTime openTime,
        LocalTime closeTime,
        String businessDays,
        BigDecimal newTariff
) {
}
