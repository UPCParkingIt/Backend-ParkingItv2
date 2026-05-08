package com.parkingit.cloud.payments.domain.services;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.domain.model.commands.*;

import java.util.Optional;

public interface PaymentCommandService {
    Optional<Payment> handle(InitiatePaymentCommand command);
    Optional<Payment> handle(CompletePaymentCommand command);
    Optional<Payment> handle(RefundPaymentCommand command);
    void handleFailedPayment(String paymentId, String failureReason);

    Optional<Payment> handle(InitiateExitPaymentCommand command);
    Optional<Payment> handle(ApprovePaymentCommand command);
    Optional<Payment> handle(RejectPaymentCommand command);
    Optional<Payment> handle(MarkDriverPaidCommand command);
    Optional<Payment> handle(AllowDriverExitCommand command);
}
