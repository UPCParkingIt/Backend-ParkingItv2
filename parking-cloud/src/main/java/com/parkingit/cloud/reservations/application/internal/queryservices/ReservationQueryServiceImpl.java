package com.parkingit.cloud.reservations.application.internal.queryservices;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsByUserIdQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetReservationByIdQuery;
import com.parkingit.cloud.reservations.domain.services.ReservationQueryService;
import com.parkingit.cloud.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ReservationQueryServiceImpl implements ReservationQueryService {
    private final ReservationRepository reservationRepository;

    @Override
    public Optional<Reservation> handle(GetReservationByIdQuery query) {
        return reservationRepository.findById(query.id());
    }

    @Override
    public List<Reservation> handle(GetAllReservationsQuery query) {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByParkingIdQuery query) {
        return reservationRepository.findAllByParkingId(query.parkingId());
    }

    @Override
    public List<Reservation> handle(GetAllReservationsByUserIdQuery query) {
        return reservationRepository.findAllByUserId(query.userId());
    }
}
