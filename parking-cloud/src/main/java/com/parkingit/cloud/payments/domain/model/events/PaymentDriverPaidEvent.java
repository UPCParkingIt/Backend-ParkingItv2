package com.parkingit.cloud.payments.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentDriverPaidEvent implements DomainEvent {
    private UUID paymentId;
    private UUID reservationId;
    private String referenceNumber;
    private Instant occurredAt;

    public PaymentDriverPaidEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }

    @Override
    public String getEventType() {
        return "payment.driver_paid";
    }
}
