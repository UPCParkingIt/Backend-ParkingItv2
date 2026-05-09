package com.parkingit.cloud.parking.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalTime;

public record UpdateParkingResource(
        String parkingName,
        Double latitude,
        Double longitude,
        String address,
        LocalTime openTime,
        LocalTime closeTime,
        String businessDays,
        BigDecimal newTariff,
        BigDecimal newReservationFee
) {
}
