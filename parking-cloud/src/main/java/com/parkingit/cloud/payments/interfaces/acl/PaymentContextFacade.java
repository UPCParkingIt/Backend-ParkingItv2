package com.parkingit.cloud.payments.interfaces.acl;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentContextFacade {
    UUID initiatePayment(UUID reservationId, BigDecimal amount, PaymentMethod paymentMethod, String description);
    Optional<Payment> fetchPaymentByReservationId(UUID reservationId);
    Boolean hasCompletedPayment(UUID reservationId);
}
