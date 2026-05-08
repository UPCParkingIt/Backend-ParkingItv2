package com.parkingit.cloud.iam.application.internal.outboundservices.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.notifications.interfaces.acl.NotificationContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("iamNotificationService")
@AllArgsConstructor
@Slf4j
public class ExternalNotificationService {
    private final NotificationContextFacade notificationContextFacade;

    //TODO: Cambiar por URL de la vista para recuperar tu contraseña
    private static final String RECOVERY_URL = "http://localhost:8080/authentication/reset-password?token=";
    private static final String SUPPORT_EMAIL = "soporte@parkingit.pe";
    private static final String WHATSAPP_SUPPORT = "+51 939 316 135";

    public void sendPasswordRecoveryEmail(
            User user,
            String firstName,
            String email,
            String recoveryToken
    ) {
        try {
            log.info("[ExternalNotificationService] Sending password recovery email to user: {} — Email: {}", user.getPersonName(), email);

            String emailBody = buildPasswordRecoveryEmailBody(firstName, recoveryToken);

            notificationContextFacade.createNotification(
                    user,
                    "Recupera tu contraseña - ParkingIT",
                    emailBody,
                    null,
                    true
            );

            log.info("[ExternalNotificationService] Password recovery email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("[ExternalNotificationService] Error sending password recovery email: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending password recovery email: " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetSuccessEmail(
            User user,
            String firstName,
            String email
    ) {
        try {
            log.info("[ExternalNotificationService] Sending password reset success email to user: {} — Email: {}", user.getPersonName(), email);

            String emailBody = buildPasswordResetSuccessEmailBody(firstName);

            notificationContextFacade.createNotification(
                    user,
                    "Contraseña cambiada exitosamente - ParkingIT",
                    emailBody,
                    null,
                    true
            );

            log.info("[ExternalNotificationService] Password reset success email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("[ExternalNotificationService] Error sending password reset success email: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending password reset success email: " + e.getMessage(), e);
        }
    }

    private String buildPasswordRecoveryEmailBody(String firstName, String recoveryToken) {
        return String.format(
                "¡Hola %s!\n\n" +
                        "Hemos recibido una solicitud para recuperar tu contraseña en ParkingIT.\n\n" +
                        "🔐 INSTRUCCIONES:\n" +
                        "1️⃣ Haz clic en el enlace de abajo\n" +
                        "2️⃣ Ingresa el token de confirmación\n" +
                        "3️⃣ Ingresa tu nueva contraseña\n\n" +
                        "🔗 Enlace de recuperación:\n" +
                        "%s\n\n" +
                        "✅ Token:\n" +
                        "%s\n\n" +
                        "⏰ Nota: Este token expira en 24 horas por seguridad.\n\n" +
                        "⚠️ SI NO SOLICITASTE ESTE CAMBIO:\n" +
                        "Ignora este email o contacta inmediatamente a nuestro equipo de soporte.\n" +
                        "📧 Email: %s\n" +
                        "📞 WhatsApp: %s\n\n" +
                        "Saludos,\n" +
                        "Equipo ParkingIT 🚗",
                firstName, RECOVERY_URL, recoveryToken, SUPPORT_EMAIL, WHATSAPP_SUPPORT
        );
    }

    private String buildPasswordResetSuccessEmailBody(String firstName) {
        return String.format(
                "¡Hola %s!\n\n" +
                        "¡Excelente! Tu contraseña ha sido cambiada exitosamente.\n\n" +
                        "✅ Tu contraseña fue actualizada correctamente.\n\n" +
                        "🔐 RECOMENDACIONES DE SEGURIDAD:\n" +
                        "• Nunca compartas tu contraseña\n" +
                        "• Usa una contraseña única y segura\n" +
                        "• Cierra sesión en otros dispositivos después del cambio\n\n" +
                        "✨ Ya puedes iniciar sesión con tu nueva contraseña en ParkingIT.\n\n" +
                        "¿Necesitas ayuda?\n" +
                        "📧 Email: %s\n" +
                        "📞 WhatsApp: %s\n\n" +
                        "Saludos,\n" +
                        "Equipo ParkingIT 🚗",
                firstName, SUPPORT_EMAIL, WHATSAPP_SUPPORT
        );
    }
}
