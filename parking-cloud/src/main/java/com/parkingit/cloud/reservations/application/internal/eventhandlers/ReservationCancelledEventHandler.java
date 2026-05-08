package com.parkingit.cloud.reservations.application.internal.eventhandlers;

import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.reservations.domain.model.events.ReservationCancelledEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationCancelledEventHandler {
    private final ExternalIamService externalIamService;
    private final ExternalNotificationService externalNotificationService;

    @EventListener(ReservationCancelledEvent.class)
    public void on(ReservationCancelledEvent event) {
        log.info("[ReservationCancelledEvent] Reservation cancelled: id={}, reason={}, cancellationTime={}",
                event.getReservationId(),
                event.getReason(),
                event.getOccurredAt()
        );

        try {
            var userOpt = externalIamService.fetchUserById(event.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("[ReservationCancelledEvent] User not found for id={}, skipping email notification", event.getUserId());
                return;
            }

            externalNotificationService.sendReservationCancelledEmail(userOpt.get(), event.getReason());
            log.info("[ReservationCancelledEvent] Cancellation email sent: reservationId={}", event.getReservationId());
        } catch (Exception e) {
            log.error("[ReservationCancelledEvent] Error processing cancellation notification: reservationId={}", event.getReservationId(), e);
        }
    }
}
