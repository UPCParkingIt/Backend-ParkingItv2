package com.parkingit.cloud.logs.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ExitLogRecordedEvent implements DomainEvent {
    private UUID parkingLogId;
    private UUID parkingId;
    private String licensePlate;
    private Boolean isMatched;
    private Long occupancyDurationMinutes;
    private Instant occurredAt;

    public ExitLogRecordedEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return parkingLogId;
    }

    @Override
    public String getEventType() {
        return "log.exit_recorded";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
