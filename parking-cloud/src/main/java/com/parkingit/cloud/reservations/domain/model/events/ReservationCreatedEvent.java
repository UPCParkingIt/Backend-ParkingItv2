package com.parkingit.cloud.reservations.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReservationCreatedEvent implements DomainEvent {
    private UUID reservationId;
    private UUID userId;
    private UUID parkingId;
    private String accessCode;
    private LocalDateTime reservedFromTime;
    private LocalDateTime accessCodeExpiresAt;
    private Instant occurredAt;

    public ReservationCreatedEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return reservationId;
    }

    @Override
    public String getEventType() {
        return "reservation.created";
    }
}
