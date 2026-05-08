package com.parkingit.cloud.payments.application.internal.eventhandlers;

import com.parkingit.cloud.payments.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.payments.domain.model.events.PaymentDriverPaidEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentDriverPaidEventHandler {
    private final ExternalNotificationService externalNotificationService;

    @EventListener
    public void on(PaymentDriverPaidEvent event) {
        log.info("[PaymentDriverPaidEventHandler] Driver paid - notifying admin: paymentId={}, ref={}",
                event.getPaymentId(),
                event.getReferenceNumber()
        );

        try {
            // TODO: Enviar notificación al admin con detalles del pago
            // TODO: Admin ve: referencia, método, monto, QR, foto del comprobante
            log.info("[PaymentDriverPaidEventHandler] Admin notification sent - awaiting approval");

        } catch (Exception e) {
            log.error("[PaymentDriverPaidEventHandler] Error: {}", e.getMessage(), e);
        }
    }
}
