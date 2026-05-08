package com.parkingit.cloud.parking.domain.model.events;

import com.parkingit.shared.domain.events.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PromotionCreatedEvent implements DomainEvent {
    private final UUID promotionId;
    private final UUID parkingId;
    private final String promotionTitle;
    private final Instant occurredAt;

    public PromotionCreatedEvent(UUID promotionId, UUID parkingId, String title) {
        this.promotionId = promotionId;
        this.parkingId = parkingId;
        this.promotionTitle = title;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return parkingId;
    }

    @Override
    public String getEventType() {
        return "promotion.created";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
