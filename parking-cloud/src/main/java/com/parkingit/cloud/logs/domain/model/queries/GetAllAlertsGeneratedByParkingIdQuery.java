package com.parkingit.cloud.logs.domain.model.queries;

import java.util.UUID;

public record GetAllAlertsGeneratedByParkingIdQuery(UUID parkingId) {
}
