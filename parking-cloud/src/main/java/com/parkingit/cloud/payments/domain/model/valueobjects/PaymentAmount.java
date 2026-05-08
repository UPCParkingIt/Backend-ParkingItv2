package com.parkingit.cloud.payments.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAmount {
    private BigDecimal amount;
    private String currency;

    public static PaymentAmount create(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }
        return new PaymentAmount(amount, "PEN");
    }

    public BigDecimal calculateWithCommission(double commissionPercentage) {
        if (commissionPercentage < 0) {
            throw new IllegalArgumentException("Commission cannot be negative");
        }
        BigDecimal commission = amount.multiply(BigDecimal.valueOf(commissionPercentage / 100));
        return amount.add(commission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentAmount that = (PaymentAmount) o;
        return Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
