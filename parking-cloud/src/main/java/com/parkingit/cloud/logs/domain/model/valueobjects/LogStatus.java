package com.parkingit.cloud.logs.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum LogStatus {
    ENTRY("Entry recorded"),
    EXIT("Exit recorded"),
    MATCHED("Facial match verified"),
    MISMATCH("Facial mismatch detected"),
    ALERT("Alert generated");

    private final String description;

    LogStatus(String description) {
        this.description = description;
    }
}
