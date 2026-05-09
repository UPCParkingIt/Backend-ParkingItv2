package com.parkingit.edge.recognition.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum RecognitionStatus {
    INACTIVE("Sensores inactivos, no procesa"),
    ACTIVE("Sensores activos, esperando detección"),
    SCANNING("Cámaras escaneando"),
    DETECTED("Objetos detectados");

    private final String description;

    RecognitionStatus(String description) {
        this.description = description;
    }
}
