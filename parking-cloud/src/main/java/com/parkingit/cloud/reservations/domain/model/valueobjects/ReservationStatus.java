package com.parkingit.cloud.reservations.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("Pending claim at parking"),
    ACTIVE("Currently using parking"),
    COMPLETED("Exit completed"),
    CANCELLED("Reservation cancelled"),
    EXPIRED("Reservation expired - arrived too late");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }
}
