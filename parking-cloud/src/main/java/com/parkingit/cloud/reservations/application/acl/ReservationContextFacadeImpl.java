package com.parkingit.cloud.reservations.application.acl;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.domain.model.commands.CancelReservationCommand;
import com.parkingit.cloud.reservations.domain.model.commands.CreateReservationCommand;
import com.parkingit.cloud.reservations.domain.model.queries.GetReservationByIdQuery;
import com.parkingit.cloud.reservations.domain.services.ReservationCommandService;
import com.parkingit.cloud.reservations.domain.services.ReservationQueryService;
import com.parkingit.cloud.reservations.interfaces.acl.ReservationContextFacade;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationContextFacadeImpl implements ReservationContextFacade {
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @Override
    public UUID createReservation(UUID userId, UUID parkingId, LocalDateTime reservedFromTime) {
        var command = new CreateReservationCommand(userId, parkingId, reservedFromTime);
        var result = reservationCommandService.handle(command);
        return result.map(AuditableAbstractAggregateRoot::getId).orElse(null);
    }

    @Override
    public Optional<Reservation> fetchReservationById(UUID id) {
        var query = new GetReservationByIdQuery(id);
        return reservationQueryService.handle(query);
    }

    @Override
    public Boolean cancelReservation(UUID reservationId, String reason) {
        var command = new CancelReservationCommand(reservationId, reason);
        reservationCommandService.handle(command);
        return true;
    }
}
