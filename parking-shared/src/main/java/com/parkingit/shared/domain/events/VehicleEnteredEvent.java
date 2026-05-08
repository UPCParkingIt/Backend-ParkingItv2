package com.parkingit.shared.domain.events;

import com.parkingit.shared.domain.valueobjects.FacialSimilarityScore;
import com.parkingit.shared.domain.valueobjects.FacialEmbedding;
import com.parkingit.shared.domain.valueobjects.LicensePlate;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class VehicleEnteredEvent implements DomainEvent {
    private final UUID vehicleEntryId;
    private final UUID parkingId;
    private final LicensePlate licensePlate;
    private final FacialEmbedding facialEmbedding;
    private final FacialSimilarityScore facialConfidence;
    private final String edgeDeviceId;
    private final Instant occurredAt;

    public VehicleEnteredEvent(
            UUID vehicleEntryId,
            UUID parkingId,
            LicensePlate licensePlate,
            FacialEmbedding facialEmbedding,
            FacialSimilarityScore facialConfidence,
            String edgeDeviceId
    ) {
        this.vehicleEntryId = vehicleEntryId;
        this.parkingId = parkingId;
        this.licensePlate = licensePlate;
        this.facialEmbedding = facialEmbedding;
        this.facialConfidence = facialConfidence;
        this.edgeDeviceId = edgeDeviceId;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return vehicleEntryId;
    }

    @Override
    public String getEventType() {
        return "vehicle.entered";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}
