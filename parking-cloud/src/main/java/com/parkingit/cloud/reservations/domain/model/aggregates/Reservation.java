package com.parkingit.cloud.reservations.domain.model.aggregates;

import com.parkingit.cloud.reservations.domain.model.valueobjects.AccessCode;
import com.parkingit.cloud.reservations.domain.model.valueobjects.ReservationStatus;
import com.parkingit.cloud.reservations.domain.model.valueobjects.ReservationTimeSlot;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Reservation extends AuditableAbstractAggregateRoot<Reservation> {

    private static final int GRACE_PERIOD_MINUTES = 15;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "parking_id", nullable = false)
    private UUID parkingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Embedded
    private ReservationTimeSlot timeSlot;

    @Embedded
    private AccessCode accessCode;

    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    @Column(name = "reservation_fee")
    private BigDecimal reservationFee;


    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public static Reservation create(
            UUID userId,
            UUID parkingId,
            ReservationTimeSlot timeSlot,
            BigDecimal reservationFee
    ) {
        if (userId == null || parkingId == null || timeSlot == null) {
            throw new IllegalArgumentException("userId, parkingId and timeSlot cannot be null");
        }

        LocalDateTime accessCodeExpiresAt = timeSlot.getReservedFromTime().plusMinutes(GRACE_PERIOD_MINUTES);

        Reservation reservation = new Reservation();
        reservation.userId = userId;
        reservation.parkingId = parkingId;
        reservation.status = ReservationStatus.PENDING;
        reservation.timeSlot = timeSlot;
        reservation.accessCode = AccessCode.generate(accessCodeExpiresAt);
        reservation.reservationFee = reservationFee;
        reservation.cancellationReason = null;

        return reservation;
    }

    /**
     * Claims the reservation at the establishment entrance.
     * Validates the access code and that the user arrived within the grace period.
     * Sets entryTime (billing starts) and transitions status to ACTIVE.
     *
     * @param providedCode the code the user presents at the entrance
     * @throws IllegalStateException if the reservation is not PENDING
     * @throws IllegalArgumentException if the code is wrong
     * @throws IllegalStateException if the user arrived after the grace period (auto-cancels)
     */
    public void claim(String providedCode) {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be claimed");
        }

        LocalDateTime gracePeriodDeadline = timeSlot.getReservedFromTime().plusMinutes(GRACE_PERIOD_MINUTES);
        if (LocalDateTime.now().isAfter(gracePeriodDeadline)) {
            this.status = ReservationStatus.EXPIRED;
            this.isActive = false;
            this.cancellationReason = "Arrived after the " + GRACE_PERIOD_MINUTES + "-minute grace period";
            throw new IllegalStateException(
                "Reservation expired: the " + GRACE_PERIOD_MINUTES + "-minute grace period has passed. " +
                "Your reserved spot has been released."
            );
        }

        if (accessCode == null || !accessCode.getCode().equals(providedCode)) {
            throw new IllegalArgumentException("Invalid access code");
        }

        if (!accessCode.isValid()) {
            throw new IllegalStateException("Access code has already been used or has expired");
        }

        accessCode.markAsUsed();
        this.entryTime = LocalDateTime.now();
        this.status = ReservationStatus.ACTIVE;
    }

    public void completeReservation() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Only active reservations can be completed");
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public void cancel(String reason) {
        if (status == ReservationStatus.COMPLETED || status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel completed or already cancelled reservations");
        }
        this.status = ReservationStatus.CANCELLED;
        this.isActive = false;
        this.cancellationReason = reason;
    }

    public boolean isExpiredByLateArrival() {
        if (status != ReservationStatus.PENDING) return false;
        LocalDateTime gracePeriodDeadline = timeSlot.getReservedFromTime().plusMinutes(GRACE_PERIOD_MINUTES);
        return LocalDateTime.now().isAfter(gracePeriodDeadline);
    }

}
