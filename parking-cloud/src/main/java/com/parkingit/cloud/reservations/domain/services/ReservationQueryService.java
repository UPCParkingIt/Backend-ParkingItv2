package com.parkingit.cloud.reservations.domain.services;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsByUserIdQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetReservationByIdQuery;

import java.util.List;
import java.util.Optional;

public interface ReservationQueryService {
    Optional<Reservation> handle(GetReservationByIdQuery query);
    List<Reservation> handle(GetAllReservationsQuery query);
    List<Reservation> handle(GetAllReservationsByParkingIdQuery query);
    List<Reservation> handle(GetAllReservationsByUserIdQuery query);
}
