package com.parkingit.cloud.iam.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CompanionAddedEvent implements DomainEvent {
    private final UUID userId;
    private final UUID companionId;
    private final String companionName;
    private final Instant occurredAt;

    public CompanionAddedEvent(UUID userId, UUID companionId, String companionName) {
        this.userId = userId;
        this.companionId = companionId;
        this.companionName = companionName;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return userId;
    }

    @Override
    public String getEventType() {
        return "user.companion_added";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
