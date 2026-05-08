package com.parkingit.shared.domain.events;

import com.parkingit.shared.domain.valueobjects.FacialSimilarityScore;
import com.parkingit.shared.domain.valueobjects.FacialEmbedding;
import com.parkingit.shared.domain.valueobjects.LicensePlate;
import com.parkingit.shared.domain.valueobjects.VehicleEntryStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class VehicleExitedEvent implements DomainEvent {
    private final UUID vehicleEntryId;
    private final UUID parkingId;
    private final LicensePlate licensePlate;
    private final FacialEmbedding exitFacialEmbedding;
    private final FacialSimilarityScore exitFacialConfidence;
    private final Float matchScore;
    private final VehicleEntryStatus finalStatus;
    private final Instant occurredAt;

    public VehicleExitedEvent(
            UUID vehicleEntryId,
            UUID parkingId,
            LicensePlate licensePlate,
            FacialEmbedding exitFacialEmbedding,
            FacialSimilarityScore exitFacialConfidence,
            Float matchScore,
            VehicleEntryStatus finalStatus
    ) {
        this.vehicleEntryId = vehicleEntryId;
        this.parkingId = parkingId;
        this.licensePlate = licensePlate;
        this.exitFacialEmbedding = exitFacialEmbedding;
        this.exitFacialConfidence = exitFacialConfidence;
        this.matchScore = matchScore;
        this.finalStatus = finalStatus;
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID getAggregateId() {
        return vehicleEntryId;
    }

    @Override
    public String getEventType() {
        return "vehicle.exited";
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}