package com.parkingit.cloud.logs.application.internal.outboundservices.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.notifications.interfaces.acl.NotificationContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("logsNotificationService")
@AllArgsConstructor
@Slf4j
public class ExternalNotificationService {
    private final NotificationContextFacade notificationContextFacade;

    public void notifyAdminAlert(
                User adminUser,
                String alertReason,
                String licensePlate
    ) {
        try {
            log.info("[ExternalNotificationService] Sending admin alert email to user: {} — Email: {}", adminUser.getPersonName(), adminUser.getEmail());

            String emailBody = String.format(
                    "¡Alerta de seguridad en ParkingIT!\n\n" +
                    "Se ha detectado una posible amenaza de seguridad relacionada con el vehículo con placa %s.\n\n" +
                    "Motivo de la alerta: %s\n\n" +
                    "Recomendamos revisar las cámaras de seguridad y tomar las medidas necesarias para garantizar la seguridad del estacionamiento.\n\n" +
                    "Saludos,\n" +
                    "Sistema de Monitoreo de ParkingIT 🚗",
                    licensePlate, alertReason
            );

            notificationContextFacade.createNotification(
                    adminUser,
                    "Placa: " + licensePlate,
                    "Razón: " + alertReason,
                    null,
                    false
            );

            log.info("[ExternalNotificationService] Admin alert email sent successfully to: {}", adminUser.getEmail());
        } catch (Exception e) {
            log.error("[ExternalNotificationService] Error sending admin alert email: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending admin alert email: " + e.getMessage(), e);
        }
    }
}
