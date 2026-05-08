package com.parkingit.cloud.payments.interfaces.rest.transform;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.interfaces.rest.resources.PaymentResource;

public class PaymentResourceFromEntityAssembler {
    public static PaymentResource toResourceFromEntity(Payment entity) {
        return new PaymentResource(
                entity.getId(),
                entity.getReservationId(),
                entity.getAmount().getAmount(),
                entity.getAmount().getCurrency(),
                entity.getPaymentMethod(),
                entity.getStatus(),
                entity.getReferenceNumber(),
                entity.getExternalTransactionId(),
                entity.getInitiatedAt(),
                entity.getCompletedAt(),
                entity.getRefundedAt(),
                entity.getFailureReason(),
                entity.getRetryCount()
        );
    }
}
