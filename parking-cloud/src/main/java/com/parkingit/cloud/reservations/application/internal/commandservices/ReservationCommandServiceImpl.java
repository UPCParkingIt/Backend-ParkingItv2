package com.parkingit.cloud.reservations.application.internal.commandservices;

import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalParkingService;
import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.domain.model.commands.*;
import com.parkingit.cloud.reservations.domain.model.events.ReservationActivatedEvent;
import com.parkingit.cloud.reservations.domain.model.events.ReservationCancelledEvent;
import com.parkingit.cloud.reservations.domain.model.events.ReservationCreatedEvent;
import com.parkingit.cloud.reservations.domain.model.valueobjects.ReservationStatus;
import com.parkingit.cloud.reservations.domain.model.valueobjects.ReservationTimeSlot;
import com.parkingit.cloud.reservations.domain.services.ReservationCommandService;
import com.parkingit.cloud.reservations.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationCommandServiceImpl implements ReservationCommandService {
    private final ReservationRepository reservationRepository;
    private final ExternalIamService externalIamService;
    private final ExternalParkingService externalParkingService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Optional<Reservation> handle(CreateReservationCommand command) {
        if (command.userId() == null || command.parkingId() == null || command.reservedFromTime() == null) {
            throw new IllegalArgumentException("userId, parkingId and reservedFromTime cannot be null");
        }

        log.info("[ReservationCommandService] Creating reservation: userId={}, parkingId={}, from={}", command.userId(), command.parkingId(), command.reservedFromTime());

        boolean overlap = reservationRepository.existsActiveOrPendingReservation(
                command.userId(),
                command.parkingId(),
                List.of(ReservationStatus.PENDING, ReservationStatus.ACTIVE)
        );
        if (overlap) {
            throw new IllegalStateException(
                "You already have an active or pending reservation for this parking lot. " +
                "Please cancel it before creating a new one."
            );
        }

        try {
            var timeSlot = ReservationTimeSlot.create(command.reservedFromTime());
            var reservation = Reservation.create(
                    command.userId(),
                    command.parkingId(),
                    timeSlot,
                    command.reservationFee()
            );

            reservation = reservationRepository.save(reservation);

            eventPublisher.publishEvent(new ReservationCreatedEvent(
                    reservation.getId(),
                    reservation.getUserId(),
                    reservation.getParkingId(),
                    reservation.getAccessCode().getCode(),
                    reservation.getTimeSlot().getReservedFromTime(),
                    reservation.getAccessCode().getExpiresAt(),
                    reservation.getCreatedAt().toInstant()
            ));

            log.info("[ReservationCommandService] Reservation created: id={}, code={}", reservation.getId(), reservation.getAccessCode().getCode());

            return Optional.of(reservation);
        } catch (IllegalArgumentException e) {
            log.error("[ReservationCommandService] Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ReservationCommandService] Error creating reservation: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public void handle(DeactivateReservationCommand command) {
        try {
            var optReservation = reservationRepository.findById(command.id());
            if (optReservation.isEmpty()) {
                throw new IllegalArgumentException("Reservation not found with ID: " + command.id());
            }
            var reservation = optReservation.get();
            reservation.setIsActive(false);
            reservationRepository.save(reservation);
        } catch (Exception e) {
            throw new RuntimeException("Error while deactivating reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public void handle(CancelReservationCommand command) {
        if (command.reservationId() == null || command.reason() == null || command.reason().isBlank()) {
            throw new IllegalArgumentException("Invalid command parameters");
        }

        log.info("[ReservationCommandService] Cancelling reservation: id={}, reason={}",
                command.reservationId(), command.reason());

        Reservation reservation = reservationRepository.findById(command.reservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + command.reservationId()));

        try {
            reservation.cancel(command.reason());
            reservationRepository.save(reservation);

            eventPublisher.publishEvent(new ReservationCancelledEvent(
                    reservation.getId(),
                    reservation.getUserId(),
                    reservation.getParkingId(),
                    command.reason(),
                    reservation.getUpdatedAt().toInstant()
            ));

            log.info("[ReservationCommandService] Reservation cancelled: id={}", reservation.getId());
        } catch (IllegalStateException e) {
            log.error("[ReservationCommandService] Cannot cancel reservation: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Optional<Reservation> handle(ClaimReservationCommand command) {
        if (command.accessCode() == null || command.accessCode().isBlank()) {
            throw new IllegalArgumentException("Access code cannot be empty");
        }

        log.info("[ReservationCommandService] Claiming reservation by code={}", command.accessCode());

        Reservation reservation = reservationRepository.findPendingByAccessCode(command.accessCode())
                .orElseThrow(() -> new IllegalArgumentException(
                    "No pending reservation found for code: " + command.accessCode() +
                    ". The code may be wrong, already used, or the reservation was cancelled."
                ));

        try {
            reservation.claim(command.accessCode());
            reservation = reservationRepository.save(reservation);

            eventPublisher.publishEvent(new ReservationActivatedEvent(
                    reservation.getId(),
                    reservation.getUserId(),
                    reservation.getParkingId(),
                    reservation.getUpdatedAt().toInstant()
            ));

            log.info("[ReservationCommandService] Reservation claimed: id={}, entryTime={}",
                    reservation.getId(), reservation.getEntryTime());

            return Optional.of(reservation);
        } catch (IllegalStateException e) {
            if (reservation.getStatus() == ReservationStatus.EXPIRED) {
                reservationRepository.save(reservation);
                log.warn("[ReservationCommandService] Reservation expired (late arrival): id={}", reservation.getId());
            }
            throw e;
        }
    }
}
