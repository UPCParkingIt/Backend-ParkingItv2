package com.parkingit.cloud.iam.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserDeactivatedEvent implements DomainEvent {
    private final UUID userId;
    private final String reason;
    private final Instant occurredAt;

    public UserDeactivatedEvent(UUID userId, String reason) {
        this.userId = userId;
        this.reason = reason;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return userId;
    }

    @Override
    public String getEventType() {
        return "user.deactivated";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
