package com.parkingit.shared.domain.valueobjects;

import lombok.Getter;

@Getter
public enum VehicleEntryStatus {
    ENTERED("Vehicle has entered the parking"),
    EXITED("Vehicle has successfully exited"),
    ALARMED("Facial recognition mismatch - requires manual verification"),
    MANUAL_OVERRIDE("Admin has manually approved the action");

    private final String description;

    VehicleEntryStatus(String description) {
        this.description = description;
    }

}