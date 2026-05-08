package com.parkingit.cloud.reservations.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.domain.model.valueobjects.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByParkingId(UUID parkingId);

    List<Reservation> findAllByUserId(UUID userId);

    /**
     * Looks up a PENDING reservation by its embedded access code value.
     * Used by the claim endpoint — the operator only knows the code, not the reservation ID.
     */
    @Query("SELECT r FROM Reservation r WHERE r.accessCode.code = :code AND r.status = 'PENDING'")
    Optional<Reservation> findPendingByAccessCode(@Param("code") String code);

    /**
     * Checks whether the user already has an active or pending reservation
     * for the given parking lot. Prevents double-booking the same spot.
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
           "WHERE r.userId = :userId " +
           "AND r.parkingId = :parkingId " +
           "AND r.status IN :activeStatuses")
    boolean existsActiveOrPendingReservation(
            @Param("userId") UUID userId,
            @Param("parkingId") UUID parkingId,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses
    );
}
