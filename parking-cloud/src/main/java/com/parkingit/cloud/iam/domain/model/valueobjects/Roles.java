package com.parkingit.cloud.iam.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum Roles {
    ADMIN_ROLE("Administrator", "Manage parking, alerts, reports"),
    USER_ROLE("User", "Make reservations, view history"),
    GUEST_ROLE("Guest", "Access without registration");

    private final String displayName;
    private final String description;

    Roles(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
