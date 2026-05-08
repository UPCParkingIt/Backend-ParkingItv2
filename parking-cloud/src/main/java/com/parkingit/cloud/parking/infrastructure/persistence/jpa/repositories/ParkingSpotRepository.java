package com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.parking.domain.model.entities.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {
    int findAllByParkingId(UUID parkingId);
}
