package com.parkingit.cloud.logs.interfaces.rest.transform;

import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import com.parkingit.cloud.logs.interfaces.rest.resources.AlertLogResource;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;

public class AlertLogResourceFromEntityAssembler {
    public static AlertLogResource toResourceFromEntity(ParkingLog entity) {
        return new AlertLogResource(
                entity.getId(),
                entity.getLicensePlate().getValue(),
                entity.getParkingId(),
                entity.getAlertReason(),
                AlertType.FACIAL_MISMATCH,
                entity.getCreatedAt().toInstant()
        );
    }
}
