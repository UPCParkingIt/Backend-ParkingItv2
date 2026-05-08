package com.parkingit.cloud.logs.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EntryLogRecordedEvent implements DomainEvent {
    private UUID parkingLogId;
    private UUID parkingId;
    private UUID userId;
    private String licensePlate;
    private Instant occurredAt;

    public EntryLogRecordedEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return parkingLogId;
    }

    @Override
    public String getEventType() {
        return "log.entry_recorded";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
