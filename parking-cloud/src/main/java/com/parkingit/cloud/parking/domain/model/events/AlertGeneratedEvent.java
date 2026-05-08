package com.parkingit.cloud.parking.domain.model.events;

import com.parkingit.cloud.parking.domain.model.valueobjects.AlertSeverity;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;
import com.parkingit.shared.domain.events.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class AlertGeneratedEvent implements DomainEvent {
    private final UUID alertId;
    private final UUID parkingId;
    private final AlertType alertType;
    private final AlertSeverity severity;
    private final String description;
    private final Instant occurredAt;

    public AlertGeneratedEvent(UUID alertId, UUID parkingId, AlertType type, AlertSeverity severity, String description) {
        this.alertId = alertId;
        this.parkingId = parkingId;
        this.alertType = type;
        this.severity = severity;
        this.description = description;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return parkingId;
    }

    @Override
    public String getEventType() {
        return "alert.generated";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
