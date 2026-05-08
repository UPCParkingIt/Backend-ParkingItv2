package com.parkingit.cloud.payments.domain.services;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.domain.model.queries.GetAllPaymentsByReservationIdQuery;
import com.parkingit.cloud.payments.domain.model.queries.GetPaymentByIdQuery;
import com.parkingit.cloud.payments.domain.model.queries.GetPaymentByReferenceNumberQuery;

import java.util.List;
import java.util.Optional;

public interface PaymentQueryService {
    Optional<Payment> handle(GetPaymentByIdQuery query);
    Optional<Payment> handle(GetPaymentByReferenceNumberQuery query);
    List<Payment> handle(GetAllPaymentsByReservationIdQuery query);
}
