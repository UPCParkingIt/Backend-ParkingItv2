package com.parkingit.cloud.parking.domain.services;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.commands.*;
import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;

import java.util.Optional;

public interface ParkingCommandService {
    Optional<Parking> handle(CreateParkingCommand command);
    void handle(DeactivateParkingCommand command);
    Optional<Parking> handle(UpdateParkingCommand command);
    void handle(ConfigureScheduleCommand command);
    Optional<Alert> handle(CreateAlertCommand command);
    void handle(ReviewAlertCommand command);
    void handle(ResolveAlertCommand command);
    void handle(MarkAsFalseAlarmCommand command);
    Optional<ParkingPromotion> handle(CreatePromotionCommand command);
    void handle(DeactivatePromotionCommand command);
    void handle(OccupySpotCommand command);
    void handle(ReleaseSpotCommand command);
}
