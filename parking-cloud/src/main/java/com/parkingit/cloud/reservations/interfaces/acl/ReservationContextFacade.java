package com.parkingit.cloud.reservations.interfaces.acl;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ReservationContextFacade {
    UUID createReservation(UUID userId, UUID parkingId, LocalDateTime reservedFromTime, BigDecimal reservationFee);
    Optional<Reservation> fetchReservationById(UUID id);
    Boolean cancelReservation(UUID reservationId, String reason);
}
