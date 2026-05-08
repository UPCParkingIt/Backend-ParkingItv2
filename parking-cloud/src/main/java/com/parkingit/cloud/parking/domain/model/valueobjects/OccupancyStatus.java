package com.parkingit.cloud.parking.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum OccupancyStatus {
    AVAILABLE("Disponible"),
    OCCUPIED("Ocupado"),
    BLOCKED("Bloqueado");

    private final String displayName;

    OccupancyStatus(String displayName) {
        this.displayName = displayName;
    }
}
