package com.parkingit.cloud.payments.domain.model.events;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentInitiatedEvent implements DomainEvent {
    private UUID paymentId;
    private UUID reservationId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private Instant occurredAt;

    public PaymentInitiatedEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }

    @Override
    public String getEventType() {
        return "payment.initiated";
    }
}
