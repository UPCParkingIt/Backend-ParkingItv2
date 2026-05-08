package com.parkingit.cloud.parking.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record CreateParkingResource(
        String parkingName,
        Double latitude,
        Double longitude,
        String address,
        Integer totalSpots,
        BigDecimal baseTariffPerHour,
        String currency,
        LocalTime openTime,
        LocalTime closeTime,
        String businessDays,
        UUID adminUserId
) {
}
