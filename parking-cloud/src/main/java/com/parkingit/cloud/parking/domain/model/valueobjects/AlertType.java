package com.parkingit.cloud.parking.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum AlertType {
    FACIAL_MISMATCH("No coincidencia facial", "El rostro de salida no coincide con entrada"),
    SUSPICIOUS_DURATION("Duración sospechosa", "Vehículo dentro menos de 5 minutos"),
    UNAUTHORIZED_ACCESS("Acceso no autorizado", "Acceso sin código de reserva válido"),
    PAYMENT_FAILED("Pago fallido", "Error al procesar pago"),
    SECURITY_BREACH("Brecha de seguridad", "Intento de acceso no autorizado"),
    SYSTEM_ERROR("Error del sistema", "Error en captura o procesamiento");

    private final String displayName;
    private final String description;

    AlertType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
