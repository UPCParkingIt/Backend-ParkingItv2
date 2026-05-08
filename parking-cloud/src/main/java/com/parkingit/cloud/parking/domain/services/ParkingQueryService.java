package com.parkingit.cloud.parking.domain.services;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;
import com.parkingit.cloud.parking.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface ParkingQueryService {
    Optional<Parking> handle(GetParkingByIdQuery query);
    Optional<Parking> handle(GetParkingByAdminUserIdQuery query);
    List<Parking> handle(GetAllParkingLotsQuery query);
    List<Parking> handle(GetAllActiveParkingLotsQuery query);
    List<Parking> handle(GetAllInactiveParkingLotsQuery query);
    List<Parking> handle(GetAllParkingLotsByNameQuery query);
    List<Alert> handle(GetAllAlertsByParkingIdQuery query);
    List<Alert> handle(GetAllAlertsByParkingIdAndStatusQuery query);
    int handle(GetAllAvailableSpotsByParkingIdQuery query);
    List<ParkingPromotion> handle(GetAllPromotionsByParkingIdQuery query);
}
