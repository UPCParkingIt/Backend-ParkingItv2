package com.parkingit.cloud.parking.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum ParkingStatus {
    OPEN("Abierto", "El estacionamiento está operativo"),
    CLOSED("Cerrado", "El estacionamiento está cerrado"),
    FULL("Lleno", "No hay espacios disponibles"),
    MAINTENANCE("Mantenimiento", "En mantenimiento");

    private final String displayName;
    private final String description;

    ParkingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
