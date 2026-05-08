package com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findAllByParkingId(UUID parkingId);

    List<Alert> findAllByParkingIdAndStatus(UUID parkingId, AlertStatus status);
}
