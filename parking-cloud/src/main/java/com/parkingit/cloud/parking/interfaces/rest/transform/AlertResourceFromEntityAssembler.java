package com.parkingit.cloud.parking.interfaces.rest.transform;

import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.interfaces.rest.resources.AlertResource;

public class AlertResourceFromEntityAssembler {
    public static AlertResource toResourceFromEntity(Alert entity) {
        return new AlertResource(
                entity.getId(),
                entity.getParkingId(),
                entity.getAlertType(),
                entity.getSeverity(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getReviewedAt(),
                entity.getResolvedAt(),
                entity.getReviewerNotes()
        );
    }
}
