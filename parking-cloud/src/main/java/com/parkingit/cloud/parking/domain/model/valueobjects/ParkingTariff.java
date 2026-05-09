package com.parkingit.cloud.parking.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
public class ParkingTariff {
    @Column(name = "base_tariff_per_hour", nullable = false)
    private BigDecimal baseTariffPerHour;

    @Column(name = "tariff_currency", nullable = false, length = 3)
    private String currency = "PEN";

    @Column(name = "tariff_updated_at")
    private Long updatedAtTimestamp;

    protected ParkingTariff() {}

    public ParkingTariff(BigDecimal baseTariffPerHour, String currency) {
        if (baseTariffPerHour == null || baseTariffPerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Tariff must be greater than 0");
        }

        if (currency == null || currency.isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }

        this.baseTariffPerHour = baseTariffPerHour;
        this.currency = currency.toUpperCase();
        this.updatedAtTimestamp = System.currentTimeMillis();
    }

    public static ParkingTariff create(BigDecimal baseRatePerHour, String currency) {
        return new ParkingTariff(baseRatePerHour, currency);
    }

    public BigDecimal calculateFor(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }

        double hours = durationMinutes / 60.0;
        return baseTariffPerHour.multiply(BigDecimal.valueOf(hours));
    }

    public void update(BigDecimal newTariff) {
        if (newTariff == null || newTariff.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New tariff must be greater than 0");
        }

        this.baseTariffPerHour = newTariff;
        this.updatedAtTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingTariff that = (ParkingTariff) o;
        return Objects.equals(baseTariffPerHour, that.baseTariffPerHour) &&
                Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseTariffPerHour, currency);
    }
}
