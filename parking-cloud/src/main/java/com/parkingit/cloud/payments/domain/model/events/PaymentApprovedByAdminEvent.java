package com.parkingit.cloud.payments.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentApprovedByAdminEvent implements DomainEvent {
    private UUID paymentId;
    private UUID reservationId;
    private UUID adminId;
    private String adminNotes;
    private Instant occurredAt;

    public PaymentApprovedByAdminEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }

    @Override
    public String getEventType() {
        return "payment.approved_by_admin";
    }
}
