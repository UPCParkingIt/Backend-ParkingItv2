package com.parkingit.cloud.payments.application.internal.queryservices;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.domain.model.queries.GetAllPaymentsByReservationIdQuery;
import com.parkingit.cloud.payments.domain.model.queries.GetPaymentByIdQuery;
import com.parkingit.cloud.payments.domain.model.queries.GetPaymentByReferenceNumberQuery;
import com.parkingit.cloud.payments.domain.services.PaymentQueryService;
import com.parkingit.cloud.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentQueryServiceImpl implements PaymentQueryService {
    private final PaymentRepository paymentRepository;

    @Override
    public Optional<Payment> handle(GetPaymentByIdQuery query) {
        log.debug("[PaymentQueryService] Getting payment by ID: {}", query.id());
        return paymentRepository.findById(query.id());
    }

    @Override
    public Optional<Payment> handle(GetPaymentByReferenceNumberQuery query) {
        log.debug("[PaymentQueryService] Getting payment by reference: {}", query.referenceNumber());
        return paymentRepository.findByReferenceNumber(query.referenceNumber());
    }

    @Override
    public List<Payment> handle(GetAllPaymentsByReservationIdQuery query) {
        log.debug("[PaymentQueryService] Getting all payments for reservation: {}", query.reservationId());
        return paymentRepository.findAllByReservationId(query.reservationId());
    }
}
