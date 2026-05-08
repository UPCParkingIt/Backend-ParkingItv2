package com.parkingit.cloud.reservations.interfaces.rest.transform;

import com.parkingit.cloud.reservations.domain.model.commands.CreateReservationCommand;
import com.parkingit.cloud.reservations.interfaces.rest.resources.CreateReservationResource;

public class CreateReservationCommandFromResourceAssembler {
    public static CreateReservationCommand toCommandFromResource(CreateReservationResource resource) {
        return new CreateReservationCommand(
                resource.userId(),
                resource.parkingId(),
                resource.reservedFromTime(),
                resource.reservationFee()
        );
    }
}
