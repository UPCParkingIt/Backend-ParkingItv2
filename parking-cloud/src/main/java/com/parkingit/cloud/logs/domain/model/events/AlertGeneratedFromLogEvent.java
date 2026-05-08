package com.parkingit.cloud.logs.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AlertGeneratedFromLogEvent implements DomainEvent {
    private UUID parkingLogId;
    private UUID parkingId;
    private String licensePlate;
    private String alertReason;
    private Instant occurredAt;

    public AlertGeneratedFromLogEvent() {
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return parkingLogId;
    }

    @Override
    public String getEventType() {
        return "log.alert_generated";
    }
}
