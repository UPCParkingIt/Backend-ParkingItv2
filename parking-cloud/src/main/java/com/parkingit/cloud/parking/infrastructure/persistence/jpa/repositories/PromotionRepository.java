package com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<ParkingPromotion, UUID> {
    List<ParkingPromotion> findAllByParkingId(UUID parkingId);
}
