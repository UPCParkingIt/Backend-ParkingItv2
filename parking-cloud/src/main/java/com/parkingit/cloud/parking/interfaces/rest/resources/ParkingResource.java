package com.parkingit.cloud.parking.interfaces.rest.resources;

import com.parkingit.cloud.parking.domain.model.valueobjects.ParkingStatus;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

public record ParkingResource(
        UUID id,
        String parkingName,
        Double latitude,
        Double longitude,
        String address,
        Integer totalSpots,
        Integer availableSpots,
        Double occupancyPercentage,
        BigDecimal baseTariffPerHour,
        String currency,
        LocalTime openTime,
        LocalTime closeTime,
        String businessDays,
        ParkingStatus status,
        UUID adminUserId,
        Boolean isActive,
        Date createdAt,
        Date updatedAt
) {
}
