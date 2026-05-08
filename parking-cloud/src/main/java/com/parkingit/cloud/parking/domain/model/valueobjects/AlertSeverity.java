package com.parkingit.cloud.parking.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum AlertSeverity {
    LOW(1, "Baja", "Información general"),
    MEDIUM(2, "Media", "Requiere atención"),
    HIGH(3, "Alta", "Requiere acción inmediata"),
    CRITICAL(4, "Crítica", "Requiere intervención urgente");

    private final int level;
    private final String displayName;
    private final String description;

    AlertSeverity(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isHigherOrEqualTo(AlertSeverity other) {
        return this.level >= other.level;
    }
}
