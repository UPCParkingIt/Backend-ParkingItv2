package com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<ParkingLog, UUID> {
    List<ParkingLog> findAllByParkingId(UUID parkingId);
    List<ParkingLog> findAllByUserId(UUID userId);
    List<ParkingLog> findAllByParkingIdAndIsAlertGenerated(UUID parkingId, Boolean isAlertGenerated);
}
