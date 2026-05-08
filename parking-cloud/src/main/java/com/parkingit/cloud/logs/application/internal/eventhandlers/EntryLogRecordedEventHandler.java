package com.parkingit.cloud.logs.application.internal.eventhandlers;

import com.parkingit.cloud.logs.domain.model.events.EntryLogRecordedEvent;
import com.parkingit.cloud.parking.interfaces.acl.ParkingContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EntryLogRecordedEventHandler {
    private final ParkingContextFacade parkingContextFacade;

    @EventListener
    public void on(EntryLogRecordedEvent event) {
        log.info("[EntryLogRecordedEvent] Entry log recorded: vehicle={}, parking={}, timestamp={}",
                event.getLicensePlate(),
                event.getParkingId(),
                event.getOccurredAt()
        );

        try {
            parkingContextFacade.occupySpot(event.getParkingId(), event.getLicensePlate());
            log.info("[EntryLogRecordedEvent] Spot occupied for vehicle: {}", event.getLicensePlate());
        } catch (Exception e) {
            log.error("[EntryLogRecordedEvent] Error occupying spot for vehicle: {}", event.getLicensePlate(), e);
        }
    }
}
