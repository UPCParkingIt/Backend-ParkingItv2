package com.parkingit.cloud.parking.domain.model.queries;

import com.parkingit.cloud.parking.domain.model.valueobjects.AlertStatus;

import java.util.UUID;

public record GetAllAlertsByParkingIdAndStatusQuery(UUID parkingId, AlertStatus status) {
}
