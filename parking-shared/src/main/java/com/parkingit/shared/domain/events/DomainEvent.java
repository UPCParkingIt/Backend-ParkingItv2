package com.parkingit.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getAggregateId();
    String getEventType();
    Instant getOccurredAt();
}
