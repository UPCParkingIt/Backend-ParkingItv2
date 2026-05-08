package com.parkingit.cloud.parking.application.acl;

import com.parkingit.cloud.parking.domain.exceptions.ParkingNotFoundException;
import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.commands.CreateAlertCommand;
import com.parkingit.cloud.parking.domain.model.commands.OccupySpotCommand;
import com.parkingit.cloud.parking.domain.model.commands.ReleaseSpotCommand;
import com.parkingit.cloud.parking.domain.model.queries.GetParkingByIdQuery;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertSeverity;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;
import com.parkingit.cloud.parking.domain.services.ParkingCommandService;
import com.parkingit.cloud.parking.domain.services.ParkingQueryService;
import com.parkingit.cloud.parking.interfaces.acl.ParkingContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ParkingContextFacadeImpl implements ParkingContextFacade {
    private final ParkingCommandService parkingCommandService;
    private final ParkingQueryService parkingQueryService;

    @Override
    public Optional<Parking> fetchParkingById(UUID id) {
        return parkingQueryService.handle(new GetParkingByIdQuery(id));
    }

    @Override
    public void occupySpot(UUID parkingId, String licensePlate) {
        try {
            Parking parking = fetchParkingById(parkingId).orElseThrow(() -> new ParkingNotFoundException("Parking not found: " + parkingId));

            var command = new OccupySpotCommand(parkingId, licensePlate);
            parkingCommandService.handle(command);

            log.info("[ParkingContextFacade] Spot occupied: parking={}, vehicle={}", parkingId, licensePlate);
        } catch (Exception e) {
            log.error("[ParkingContextFacade] Error occupying spot: parking={}, vehicle={}", parkingId, licensePlate, e);
            throw new RuntimeException("Failed to occupy spot", e);
        }
    }

    @Override
    public void releaseSpot(UUID parkingId, String licensePlate) {
        try {
            Parking parking = fetchParkingById(parkingId).orElseThrow(() -> new ParkingNotFoundException("Parking not found: " + parkingId));

            var command = new ReleaseSpotCommand(parkingId, licensePlate);
            parkingCommandService.handle(command);

            log.info("[ParkingContextFacade] Spot released: parking={}, vehicle={}", parkingId, licensePlate);
        } catch (Exception e) {
            log.error("[ParkingContextFacade] Error releasing spot: parking={}, vehicle={}", parkingId, licensePlate, e);
            throw new RuntimeException("Failed to release spot", e);
        }
    }

    @Override
    public void createSecurityAlert(UUID parkingId, String reason, AlertType alertType) {
        try {
            Parking parking = fetchParkingById(parkingId).orElseThrow(() -> new ParkingNotFoundException("Parking not found: " + parkingId));

            AlertType type = AlertType.SECURITY_BREACH;
            if ("FACIAL_MISMATCH".equalsIgnoreCase(alertType.toString())) {
                type = AlertType.SECURITY_BREACH;
            }

            var command = new CreateAlertCommand(
                    parkingId,
                    type,
                    AlertSeverity.HIGH,
                    reason,
                    null
            );

            parkingCommandService.handle(command);

            log.warn("[ParkingContextFacade] Security alert created: parking={}, reason={}", parkingId, reason);
        } catch (Exception e) {
            log.error("[ParkingContextFacade] Error creating security alert: parking={}", parkingId, e);
            throw new RuntimeException("Failed to create security alert", e);
        }
    }
}
