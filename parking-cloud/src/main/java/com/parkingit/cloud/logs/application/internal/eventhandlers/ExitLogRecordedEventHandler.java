package com.parkingit.cloud.logs.application.internal.eventhandlers;

import com.parkingit.cloud.logs.domain.model.events.ExitLogRecordedEvent;
import com.parkingit.cloud.parking.interfaces.acl.ParkingContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ExitLogRecordedEventHandler {
    private final ParkingContextFacade parkingContextFacade;

    @EventListener
    public void on(ExitLogRecordedEvent event) {
        log.info("[ExitLogRecordedEvent] Exit log recorded: vehicle={}, parking={}, timestamp={}, isMatched={}",
                event.getLicensePlate(),
                event.getParkingId(),
                event.getOccurredAt(),
                event.getIsMatched());

        try {
            parkingContextFacade.releaseSpot(event.getParkingId(), event.getLicensePlate());
            log.info("[ExitLogRecordedEvent] Spot released for vehicle: {}", event.getLicensePlate());
        } catch (Exception e) {
            log.error("[ExitLogRecordedEvent] Error releasing spot for vehicle: {}", event.getLicensePlate(), e);
        }
    }
}
