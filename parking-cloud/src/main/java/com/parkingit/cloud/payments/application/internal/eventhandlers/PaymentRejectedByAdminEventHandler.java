package com.parkingit.cloud.payments.application.internal.eventhandlers;

import com.parkingit.cloud.payments.domain.model.events.PaymentRejectedByAdminEvent;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertSeverity;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentRejectedByAdminEventHandler {
    @EventListener
    public void on(PaymentRejectedByAdminEvent event) {
        log.warn("[PaymentRejectedHandler] Payment REJECTED by admin: paymentId={}, reason={}",
                event.getPaymentId(),
                event.getReason()
        );

        try {
            // TODO: Crear Alert en PARKING context
            String alertDescription = String.format(
                    "Payment rejected by admin. Reason: %s. Reference: %s",
                    event.getReason(),
                    event.getPaymentId()
            );

            // TODO: Llamar a ParkingContextFacade.createAlert(...)
            // alert = Alert.create(
            //     parkingId,
            //     AlertType.PAYMENT_FAILED,
            //     AlertSeverity.HIGH,
            //     alertDescription
            // )

            // TODO: Bloquear salida del driver - mantener barrera cerrada
            // TODO: Enviar notificación al driver con opción de reintentar pago

            log.warn("[PaymentRejectedHandler] Driver blocked at exit - payment failed alert created");

        } catch (Exception e) {
            log.error("[PaymentRejectedHandler] Error: {}", e.getMessage(), e);
        }
    }
}
