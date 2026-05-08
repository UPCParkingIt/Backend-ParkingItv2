package com.parkingit.cloud.payments.application.internal.eventhandlers;

import com.parkingit.cloud.payments.domain.model.events.PaymentApprovedByAdminEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentApprovedByAdminEventHandler {
    @EventListener
    public void on(PaymentApprovedByAdminEvent event) {
        log.info("[PaymentApprovedHandler] Payment approved by admin: paymentId={}, adminId={}",
                event.getPaymentId(),
                event.getAdminId()
        );

        try {
            // TODO: Marcar reserva como COMPLETED
            // TODO: Permitir salida del driver - abrir barrera/puerta
            // TODO: Enviar confirmación al driver

            log.info("[PaymentApprovedHandler] Driver EXIT allowed - barrier opened");

        } catch (Exception e) {
            log.error("[PaymentApprovedHandler] Error: {}", e.getMessage(), e);
        }
    }
}
