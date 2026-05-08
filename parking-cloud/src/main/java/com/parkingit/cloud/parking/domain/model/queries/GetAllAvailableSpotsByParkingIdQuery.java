package com.parkingit.cloud.parking.domain.model.queries;

import java.util.UUID;

public record GetAllAvailableSpotsByParkingIdQuery(UUID parkingId) {
}
