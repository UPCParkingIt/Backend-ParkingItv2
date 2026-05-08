package com.parkingit.cloud.payments.application.internal.eventhandlers;

import com.parkingit.cloud.payments.domain.model.events.PaymentInitiatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentInitiatedEventHandler {
    /**
     * Maneja eventos de pago iniciado
     * - Enviar confirmación al usuario
     * - Registrar en log de auditoría
     * - Iniciar polling/webhook para verificación
     */

    @EventListener
    public void on(PaymentInitiatedEvent event) {
        log.info("[PaymentInitiatedEventHandler] Payment initiated: paymentId={}, ref={}, method={}",
                event.getPaymentId(),
                event.getReferenceNumber(),
                event.getPaymentMethod()
        );

        try {
            // TODO: Enviar email/SMS al usuario con instrucciones de pago
            log.info("[PaymentInitiatedEventHandler] Payment instruction sent to user");

            // TODO: Iniciar verificación periódica (polling)
            // schedulePaymentVerification(event.getPaymentId());

        } catch (Exception e) {
            log.error("[PaymentInitiatedEventHandler] Error processing payment initiated event: {}", e.getMessage(), e);
        }
    }
}
