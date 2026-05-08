package com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingRepository extends JpaRepository<Parking, UUID> {
    List<Parking> findAllByParkingName(String parkingName);
    List<Parking> findAllByIsActiveEquals(Boolean isActive);

    boolean existsByAdminUserIdAndIsActiveTrue(UUID adminUserId, Boolean isActive);

    Optional<Parking> findByAdminUserId(UUID adminUserId);
}
