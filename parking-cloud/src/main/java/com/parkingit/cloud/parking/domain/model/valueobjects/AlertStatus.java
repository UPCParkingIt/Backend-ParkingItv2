package com.parkingit.cloud.parking.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum AlertStatus {
    PENDING("Pendiente", "Sin revisar"),
    REVIEWED("Revisada", "Administrador revisó la alerta"),
    RESOLVED("Resuelta", "Problema solucionado"),
    FALSE_ALARM("Falsa alarma", "No hay problema");

    private final String displayName;
    private final String description;

    AlertStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
