package com.parkingit.cloud.payments.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByReferenceNumber(String referenceNumber);

    List<Payment> findAllByReservationId(UUID reservationId);

    List<Payment> findAllByPaymentStage(PaymentStage paymentStage);
}
