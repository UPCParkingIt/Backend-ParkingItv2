package com.parkingit.cloud.parking.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ParkingCreatedEvent implements DomainEvent {
    private final UUID parkingId;
    private final String parkingName;
    private final UUID adminUserId;
    private final Instant occurredAt;

    public ParkingCreatedEvent(UUID parkingId, String parkingName, UUID adminUserId) {
        this.parkingId = parkingId;
        this.parkingName = parkingName;
        this.adminUserId = adminUserId;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return parkingId;
    }

    @Override
    public String getEventType() {
        return "parking.created";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
