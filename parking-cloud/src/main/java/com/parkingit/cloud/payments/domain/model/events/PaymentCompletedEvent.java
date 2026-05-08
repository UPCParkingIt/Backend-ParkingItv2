package com.parkingit.cloud.payments.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentCompletedEvent implements DomainEvent {
    private UUID paymentId;
    private UUID reservationId;
    private String externalTransactionId;
    private BigDecimal amount;
    private Instant occurredAt;

    public PaymentCompletedEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }

    @Override
    public String getEventType() {
        return "payment.completed";
    }
}
