package com.parkingit.cloud.parking.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record CreateParkingCommand(
        String parkingName,
        Double latitude,
        Double longitude,
        String address,
        Integer totalSpots,
        BigDecimal baseRatePerHour,
        String currency,
        LocalTime openTime,
        LocalTime closeTime,
        String businessDays,
        UUID adminUserId,
        BigDecimal reservationFee
) {
}
