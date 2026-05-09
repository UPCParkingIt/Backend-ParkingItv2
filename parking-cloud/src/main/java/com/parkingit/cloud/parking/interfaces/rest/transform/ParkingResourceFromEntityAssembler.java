package com.parkingit.cloud.parking.interfaces.rest.transform;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.interfaces.rest.resources.ParkingResource;

public class ParkingResourceFromEntityAssembler {
    public static ParkingResource toResourceFromEntity(Parking entity) {
        return new ParkingResource(
                entity.getId(),
                entity.getParkingName(),
                entity.getLocation().getLatitude(),
                entity.getLocation().getLongitude(),
                entity.getLocation().getAddress(),
                entity.getTotalSpots(),
                entity.getAvailableSpots(),
                entity.getOccupancyPercentage(),
                entity.getTariff().getBaseTariffPerHour(),
                entity.getTariff().getCurrency(),
                entity.getOperationSchedule().getOpenTime(),
                entity.getOperationSchedule().getCloseTime(),
                entity.getOperationSchedule().getBusinessDays(),
                entity.getStatus(),
                entity.getAdminUserId(),
                entity.getReservationFee(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
