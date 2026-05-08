package com.parkingit.cloud.reservations.application.internal.outboundservices.acl;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.interfaces.acl.ParkingContextFacade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ExternalParkingService {
    private final ParkingContextFacade parkingContextFacade;

    public Optional<Parking> fetchParkingById(UUID id) {
        return parkingContextFacade.fetchParkingById(id);
    }
}
