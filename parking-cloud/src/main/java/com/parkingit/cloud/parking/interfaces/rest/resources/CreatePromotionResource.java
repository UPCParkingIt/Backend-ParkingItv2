package com.parkingit.cloud.parking.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePromotionResource(
        UUID parkingId,
        String title,
        String description,
        BigDecimal discountPercent,
        LocalDate validFrom,
        LocalDate validTo
) {
}
