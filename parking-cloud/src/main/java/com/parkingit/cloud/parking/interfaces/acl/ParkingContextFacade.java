package com.parkingit.cloud.parking.interfaces.acl;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;

import java.util.Optional;
import java.util.UUID;

public interface ParkingContextFacade {
    Optional<Parking> fetchParkingById(UUID id);
    void occupySpot(UUID parkingId, String licensePlate);
    void releaseSpot(UUID parkingId, String licensePlate);
    void createSecurityAlert(UUID parkingId, String reason, AlertType alertType);
}
