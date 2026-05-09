package com.parkingit.cloud.reservations.interfaces.rest.transform;

import com.parkingit.cloud.reservations.domain.model.aggregates.Reservation;
import com.parkingit.cloud.reservations.interfaces.rest.resources.ReservationResource;

public class ReservationResourceFromEntityAssembler {
    public static ReservationResource toResourceFromEntity(Reservation entity) {
        return new ReservationResource(
                entity.getId(),
                entity.getUserId(),
                entity.getParkingId(),
                entity.getTimeSlot().getReservedFromTime(),
                entity.getAccessCode() != null ? entity.getAccessCode().getExpiresAt() : null,
                entity.getAccessCode() != null ? entity.getAccessCode().getCode() : null,
                entity.getStatus(),
                entity.getEntryTime(),
                entity.getCreatedAt()
        );
    }
}
