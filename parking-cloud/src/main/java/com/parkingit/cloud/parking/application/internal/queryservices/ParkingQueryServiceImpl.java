package com.parkingit.cloud.parking.application.internal.queryservices;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;
import com.parkingit.cloud.parking.domain.model.queries.*;
import com.parkingit.cloud.parking.domain.services.ParkingQueryService;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.ParkingRepository;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.ParkingSpotRepository;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.PromotionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ParkingQueryServiceImpl implements ParkingQueryService {
    private final ParkingRepository parkingRepository;
    private final AlertRepository alertRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public Optional<Parking> handle(GetParkingByIdQuery query) {
        return parkingRepository.findById(query.id());
    }

    @Override
    public Optional<Parking> handle(GetParkingByAdminUserIdQuery query) {
        return parkingRepository.findByAdminUserId(query.adminUserId());
    }

    @Override
    public List<Parking> handle(GetAllParkingLotsQuery query) {
        return parkingRepository.findAll();
    }

    @Override
    public List<Parking> handle(GetAllActiveParkingLotsQuery query) {
        return parkingRepository.findAllByIsActiveEquals(true);
    }

    @Override
    public List<Parking> handle(GetAllInactiveParkingLotsQuery query) {
        return parkingRepository.findAllByIsActiveEquals(false);
    }

    @Override
    public List<Parking> handle(GetAllParkingLotsByNameQuery query) {
        return parkingRepository.findAllByParkingName(query.parkingName());
    }

    @Override
    public List<Alert> handle(GetAllAlertsByParkingIdQuery query) {
        return alertRepository.findAllByParkingId(query.parkingId());
    }

    @Override
    public List<Alert> handle(GetAllAlertsByParkingIdAndStatusQuery query) {
        return alertRepository.findAllByParkingIdAndStatus(query.parkingId(), query.status());
    }

    @Override
    public int handle(GetAllAvailableSpotsByParkingIdQuery query) {
        return parkingSpotRepository.findAllByParkingId(query.parkingId());
    }

    @Override
    public List<ParkingPromotion> handle(GetAllPromotionsByParkingIdQuery query) {
        return promotionRepository.findAllByParkingId(query.parkingId());
    }
}
