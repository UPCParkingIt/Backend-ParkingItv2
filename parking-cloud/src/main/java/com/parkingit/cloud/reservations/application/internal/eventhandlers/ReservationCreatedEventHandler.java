package com.parkingit.cloud.reservations.application.internal.eventhandlers;

import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.reservations.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.reservations.domain.model.events.ReservationCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Service
@AllArgsConstructor
@Slf4j
public class ReservationCreatedEventHandler {
    private final ExternalIamService externalIamService;
    private final ExternalNotificationService externalNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ReservationCreatedEvent event) {
        log.info("[ReservationCreatedEvent] Reservation created: id={}, user={}, parking={}, code={}, reservedFrom={}",
                event.getReservationId(),
                event.getUserId(),
                event.getParkingId(),
                event.getAccessCode(),
                event.getReservedFromTime()
        );

        try {
            var userOpt = externalIamService.fetchUserById(event.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("[ReservationCreatedEvent] User not found for id={}, skipping notification", event.getUserId());
                return;
            }

            externalNotificationService.sendReservationCreatedEmail(
                    userOpt.get(),
                    event.getAccessCode(),
                    event.getReservedFromTime(),
                    event.getAccessCodeExpiresAt()
            );
            log.info("[ReservationCreatedEvent] Reservation email with access code sent: reservationId={}", event.getReservationId());
        } catch (Exception e) {
            log.error("[ReservationCreatedEvent] Error sending notification: reservationId={}", event.getReservationId(), e);
        }
    }
}
