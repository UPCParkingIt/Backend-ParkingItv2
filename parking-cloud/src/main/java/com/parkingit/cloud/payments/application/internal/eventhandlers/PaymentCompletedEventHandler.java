package com.parkingit.cloud.payments.application.internal.eventhandlers;

import com.parkingit.cloud.payments.domain.model.events.PaymentCompletedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentCompletedEventHandler {
    /**
     * Maneja eventos de pago completado
     * - Actualizar reserva a PAID
     * - Enviar recibo al usuario
     * - Registrar en contabilidad
     */

    @EventListener
    public void on(PaymentCompletedEvent event) {
        log.info("[PaymentCompletedEventHandler] Payment completed: paymentId={}, reservationId={}, amount={}",
                event.getPaymentId(),
                event.getReservationId(),
                event.getAmount()
        );

        try {
            // TODO: Llamar a ReservationContextFacade para marcar reserva como PAID
            // reservationContextFacade.markReservationAsPaid(event.getReservationId());

            // TODO: Enviar recibo por email
            log.info("[PaymentCompletedEventHandler] Receipt sent to user");

        } catch (Exception e) {
            log.error("[PaymentCompletedEventHandler] Error processing payment completed event: {}", e.getMessage(), e);
        }
    }
}
