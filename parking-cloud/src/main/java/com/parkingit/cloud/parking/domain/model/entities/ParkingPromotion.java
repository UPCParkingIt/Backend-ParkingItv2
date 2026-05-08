package com.parkingit.cloud.parking.domain.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParkingPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "parking_id", nullable = false)
    private UUID parkingId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_percent", nullable = false)
    private BigDecimal discountPercent;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public static ParkingPromotion create(
            UUID parkingId,
            String title,
            String description,
            BigDecimal discountPercent,
            LocalDate validFrom,
            LocalDate validTo
    ) {
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0 ||
                discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        if (validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        ParkingPromotion promotion = new ParkingPromotion();
        promotion.parkingId = parkingId;
        promotion.title = title;
        promotion.description = description;
        promotion.discountPercent = discountPercent;
        promotion.validFrom = validFrom;
        promotion.validTo = validTo;
        promotion.isActive = true;
        return promotion;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return isActive && !today.isBefore(validFrom) && !today.isAfter(validTo);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isActive()) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(discountPercent).divide(BigDecimal.valueOf(100));
    }
}
