package com.parkingit.cloud.payments.domain.model.queries;

import java.util.UUID;

public record GetAllPaymentsByReservationIdQuery(UUID reservationId) {
}
