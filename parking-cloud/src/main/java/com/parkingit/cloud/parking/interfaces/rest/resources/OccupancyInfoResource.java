package com.parkingit.cloud.parking.interfaces.rest.resources;

import com.parkingit.cloud.parking.domain.model.valueobjects.ParkingStatus;

public record OccupancyInfoResource(
        Integer availableSpots,
        Integer totalSpots,
        Double occupancyPercentage,
        ParkingStatus status
) {
}
