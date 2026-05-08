package com.parkingit.cloud.logs.application.internal.eventhandlers;

import com.parkingit.cloud.logs.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.logs.domain.model.events.AlertGeneratedFromLogEvent;
import com.parkingit.cloud.notifications.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;
import com.parkingit.cloud.parking.interfaces.acl.ParkingContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlertGeneratedFromLogEventHandler {
    private final ParkingContextFacade parkingContextFacade;
    private final ExternalNotificationService externalNotificationService;
    private final ExternalIamService externalIamService;

    @EventListener
    public void on(AlertGeneratedFromLogEvent event) {
        log.warn("[AlertGeneratedFromLogEvent] Alert generated: vehicle={}, parking={}, reason={}",
                event.getLicensePlate(),
                event.getParkingId(),
                event.getAlertReason());

        try {
            parkingContextFacade.createSecurityAlert(
                    event.getParkingId(),
                    event.getAlertReason(),
                    AlertType.FACIAL_MISMATCH
            );
            var parkingOpt = parkingContextFacade.fetchParkingById(event.getParkingId());

            if (parkingOpt.isEmpty()) {
                log.error("[AlertGeneratedFromLogEvent] Parking not found for ID: {}", event.getParkingId());
                return;
            }

            var parking = parkingOpt.get();

            var adminUserOpt = externalIamService.fetchUserById(parking.getAdminUserId());

            if (adminUserOpt.isEmpty()) {
                log.error("[AlertGeneratedFromLogEvent] Admin user not found for ID: {}", event.getParkingId());
                return;
            }

            var adminUser = adminUserOpt.get();

            externalNotificationService.notifyAdminAlert(
                    adminUser,
                    "Security Alert: " + event.getAlertReason(),
                    event.getLicensePlate()
            );

            log.info("[AlertGeneratedFromLogEvent] Alert processed successfully for vehicle: {}", event.getLicensePlate());
        } catch (Exception e) {
            log.error("[AlertGeneratedFromLogEvent] Error processing alert for vehicle: {}", event.getLicensePlate(), e);
        }
    }
}
