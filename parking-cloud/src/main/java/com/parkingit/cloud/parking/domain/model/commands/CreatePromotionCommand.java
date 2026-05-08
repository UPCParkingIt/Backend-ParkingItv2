package com.parkingit.cloud.parking.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePromotionCommand(
        UUID parkingId,
        String title,
        String description,
        BigDecimal discountPercent,
        LocalDate validFrom,
        LocalDate validTo
) {
}
