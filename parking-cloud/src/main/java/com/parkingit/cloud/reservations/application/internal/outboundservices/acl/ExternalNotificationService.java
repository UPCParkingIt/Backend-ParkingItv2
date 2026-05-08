package com.parkingit.cloud.reservations.application.internal.outboundservices.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.notifications.interfaces.acl.NotificationContextFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service("reservationNotificationService")
@AllArgsConstructor
@Slf4j
public class ExternalNotificationService {
    private final NotificationContextFacade notificationContextFacade;

    private static final String SUPPORT_EMAIL = "soporte@parkingit.pe";
    private static final String WHATSAPP_SUPPORT = "+51 939 316 135";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────
    //  Reservation Created — includes access code directly
    // ─────────────────────────────────────────────────────────────────

    /**
     * Sends the reservation confirmation email with the access code.
     *
     * @param user              the user who made the reservation
     * @param accessCode        the 8-character code to present at the parking entrance
     * @param reservedFromTime  the scheduled arrival time
     * @param accessCodeExpiresAt deadline to claim the spot (reservedFromTime + 15 min)
     */
    public void sendReservationCreatedEmail(
            User user,
            String accessCode,
            LocalDateTime reservedFromTime,
            LocalDateTime accessCodeExpiresAt
    ) {
        try {
            String firstName = user.getPersonName().getFirstName();
            String body = buildReservationCreatedBody(firstName, accessCode, reservedFromTime, accessCodeExpiresAt);

            notificationContextFacade.createNotification(
                    user,
                    "🎉 ¡Reserva confirmada! Tu código de acceso - ParkingIT",
                    body,
                    null,
                    true
            );

            log.info("[ExternalNotificationService] Reservation-created email sent to: {}", user.getUsername());
        } catch (Exception e) {
            log.error("[ExternalNotificationService] Error sending reservation-created email: {}", e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Reservation Cancelled
    // ─────────────────────────────────────────────────────────────────

    public void sendReservationCancelledEmail(User user, String reason) {
        try {
            String firstName = user.getPersonName().getFirstName();
            String body = buildReservationCancelledBody(firstName, reason);

            notificationContextFacade.createNotification(
                    user,
                    "❌ Reserva cancelada - ParkingIT",
                    body,
                    null,
                    true
            );

            log.info("[ExternalNotificationService] Reservation-cancelled email sent to: {}", user.getUsername());
        } catch (Exception e) {
            log.error("[ExternalNotificationService] Error sending reservation-cancelled email: {}", e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Reservation Activated (claim validated, user entered parking)
    // ─────────────────────────────────────────────────────────────────

    public void sendReservationActivatedEmail(User user) {
        try {
            String firstName = user.getPersonName().getFirstName();
            String body = buildReservationActivatedBody(firstName);

            notificationContextFacade.createNotification(
                    user,
                    "🚗 ¡Bienvenido al estacionamiento! - ParkingIT",
                    body,
                    null,
                    true
            );

            log.info("[ExternalNotificationService] Reservation-activated email sent to: {}", user.getUsername());
        } catch (Exception e) {
            log.error("[ExternalNotificationService] Error sending reservation-activated email: {}", e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Email body builders
    // ─────────────────────────────────────────────────────────────────

    private String buildReservationCreatedBody(
            String firstName,
            String accessCode,
            LocalDateTime reservedFromTime,
            LocalDateTime accessCodeExpiresAt
    ) {
        String arrivalTime = reservedFromTime.format(TIME_FORMAT);
        String deadline = accessCodeExpiresAt.format(TIME_FORMAT);

        return String.format(
                "¡Hola %s!%n%n" +
                "Tu reserva en ParkingIT ha sido confirmada. 🎉%n%n" +
                "🔐 CÓDIGO DE ACCESO:%n" +
                "┌──────────────────┐%n" +
                "│    %s    │%n" +
                "└──────────────────┘%n%n" +
                "📌 CÓMO USARLO:%n" +
                "1️⃣  Preséntate en la entrada del estacionamiento.%n" +
                "2️⃣  Entrega este código al operador o ingrésalo en el panel.%n" +
                "3️⃣  La tarifa por horas comenzará a contar desde el momento en que reclames tu lugar.%n%n" +
                "⏰ HORARIO DE TU RESERVA:%n" +
                "• Hora de llegada registrada: %s%n" +
                "• Límite para reclamar tu lugar: %s%n%n" +
                "⚠️  IMPORTANTE: Si llegas después de las %s, tu reserva quedará cancelada " +
                "automáticamente y el lugar será liberado.%n%n" +
                "💰 Al momento de la salida, se cobrará la tarifa por horas + el monto de la reserva.%n%n" +
                "⚠️  Este código es personal e intransferible. No lo compartas.%n%n" +
                "¿Necesitas ayuda?%n" +
                "📧 Email: %s%n" +
                "📞 WhatsApp: %s%n%n" +
                "Saludos,%n" +
                "Equipo ParkingIT 🚗",
                firstName, accessCode, arrivalTime, deadline, deadline, SUPPORT_EMAIL, WHATSAPP_SUPPORT
        );
    }

    private String buildReservationCancelledBody(String firstName, String reason) {
        return String.format(
                "¡Hola %s!%n%n" +
                "Tu reserva en ParkingIT ha sido cancelada.%n%n" +
                "📋 MOTIVO DE CANCELACIÓN:%n" +
                "• %s%n%n" +
                "Si la cancelación fue un error o necesitas realizar una nueva reserva, " +
                "puedes hacerlo en cualquier momento desde la aplicación.%n%n" +
                "¿Necesitas ayuda?%n" +
                "📧 Email: %s%n" +
                "📞 WhatsApp: %s%n%n" +
                "Saludos,%n" +
                "Equipo ParkingIT 🚗",
                firstName, reason, SUPPORT_EMAIL, WHATSAPP_SUPPORT
        );
    }

    private String buildReservationActivatedBody(String firstName) {
        return String.format(
                "¡Hola %s!%n%n" +
                "Tu acceso al estacionamiento ha sido validado exitosamente. 🎉%n%n" +
                "🚗 Ya estás dentro del estacionamiento ParkingIT.%n%n" +
                "📌 RECUERDA:%n" +
                "• La tarifa por horas ha comenzado a contar desde este momento.%n" +
                "• Respeta las señales y límites de velocidad dentro del recinto.%n" +
                "• Al salir, se cobrará el tiempo de estadía + el monto de tu reserva.%n%n" +
                "¿Necesitas ayuda?%n" +
                "📧 Email: %s%n" +
                "📞 WhatsApp: %s%n%n" +
                "Saludos,%n" +
                "Equipo ParkingIT 🚗",
                firstName, SUPPORT_EMAIL, WHATSAPP_SUPPORT
        );
    }
}
