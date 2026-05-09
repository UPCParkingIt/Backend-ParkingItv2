package com.parkingit.cloud.parking.interfaces.rest.transform;

import com.parkingit.cloud.parking.domain.model.commands.CreateParkingCommand;
import com.parkingit.cloud.parking.interfaces.rest.resources.CreateParkingResource;

public class CreateParkingCommandFromResourceAssembler {
    public static CreateParkingCommand toCommandFromResource(CreateParkingResource resource) {
        return new CreateParkingCommand(
                resource.parkingName(),
                resource.latitude(),
                resource.longitude(),
                resource.address(),
                resource.totalSpots(),
                resource.baseTariffPerHour(),
                resource.currency(),
                resource.openTime(),
                resource.closeTime(),
                resource.businessDays(),
                resource.adminUserId(),
                resource.reservationFee()
        );
    }
}
