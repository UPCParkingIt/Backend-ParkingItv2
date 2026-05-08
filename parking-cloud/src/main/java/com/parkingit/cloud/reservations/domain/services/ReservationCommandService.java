package com.parkingit.cloud.reservations.domain.services;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.domain.model.commands.*;

import java.util.Optional;

public interface ReservationCommandService {
    Optional<Reservation> handle(CreateReservationCommand command);
    void handle(DeactivateReservationCommand command);
    void handle(CancelReservationCommand command);
    Optional<Reservation> handle(ClaimReservationCommand command);
}
