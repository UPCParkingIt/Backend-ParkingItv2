package com.parkingit.cloud.payments.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentRejectedByAdminEvent implements DomainEvent {
    private UUID paymentId;
    private UUID reservationId;
    private UUID parkingId;
    private UUID adminId;
    private String reason;
    private Instant occurredAt;

    public PaymentRejectedByAdminEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }

    @Override
    public String getEventType() {
        return "payment.rejected_by_admin";
    }
}
