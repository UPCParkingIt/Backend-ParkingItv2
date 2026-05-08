package com.parkingit.cloud.reservations.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReservationCancelledEvent implements DomainEvent {
    private UUID reservationId;
    private UUID userId;
    private UUID parkingId;
    private String reason;
    private Instant occurredAt;

    public ReservationCancelledEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return reservationId;
    }

    @Override
    public String getEventType() {
        return "reservation.cancelled";
    }
}
