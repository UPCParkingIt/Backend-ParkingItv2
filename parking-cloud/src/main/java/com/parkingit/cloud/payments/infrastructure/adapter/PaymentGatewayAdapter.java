package com.parkingit.cloud.payments.infrastructure.adapter;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import java.math.BigDecimal;

public interface PaymentGatewayAdapter {
    String initiatePayment(String referenceNumber, BigDecimal amount, String userIdentifier);
    boolean verifyPayment(String externalTransactionId);
    void refundPayment(String externalTransactionId, String reason);
    PaymentMethod getMethod();
}
