package com.parkingit.cloud.reservations.application.internal.eventhandlers;

import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.reservations.domain.model.events.ReservationActivatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationActivatedEventHandler {
    private final ExternalIamService externalUserService;
    private final ExternalNotificationService externalNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ReservationActivatedEvent event) {
        log.info("[ReservationActivatedEvent] Reservation activated: id={}, userId={}, parkingId={}, occurredAt={}",
                event.getReservationId(),
                event.getUserId(),
                event.getParkingId(),
                event.getOccurredAt()
        );

        try {
            var userOpt = externalUserService.fetchUserById(event.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("[ReservationActivatedEvent] User not found for id={}, skipping email notification", event.getUserId());
                return;
            }

            externalNotificationService.sendReservationActivatedEmail(userOpt.get());
            log.info("[ReservationActivatedEvent] Welcome email sent: reservationId={}", event.getReservationId());
        } catch (Exception e) {
            log.error("[ReservationActivatedEvent] Error processing activation notification: reservationId={}", event.getReservationId(), e);
        }
    }
}
