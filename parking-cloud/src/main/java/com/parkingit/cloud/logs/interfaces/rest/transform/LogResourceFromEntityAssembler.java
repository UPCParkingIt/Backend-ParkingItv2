package com.parkingit.cloud.logs.interfaces.rest.transform;

import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import com.parkingit.cloud.logs.interfaces.rest.resources.LogResource;

import java.time.ZoneOffset;

public class LogResourceFromEntityAssembler {
    public static LogResource toResourceFromEntity(ParkingLog entity) {
        return new LogResource(
                entity.getId(),
                entity.getLicensePlate().getValue(),
                entity.getParkingId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getOccupancyDurationMinutes(),
                entity.getIsAlertGenerated(),
                entity.getAlertReason(),
                entity.getEntryLog().getEntryTimestamp().toInstant(ZoneOffset.UTC),
                entity.getExitLog().getExitTimestamp().toInstant(ZoneOffset.UTC),
                entity.getCreatedAt().toInstant()
        );
    }
}
