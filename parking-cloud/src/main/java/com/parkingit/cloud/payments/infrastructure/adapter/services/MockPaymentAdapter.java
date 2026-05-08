package com.parkingit.cloud.payments.infrastructure.adapter.services;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "payment.mock.enabled", havingValue = "true")
@Slf4j
public class MockPaymentAdapter implements PaymentGatewayAdapter {
    @Override
    public String initiatePayment(String referenceNumber, BigDecimal amount, String userIdentifier) {
        log.info("[MockPaymentAdapter] Mock payment initiated: ref={}, amount={}", referenceNumber, amount);
        return "MOCK_TXN_" + System.nanoTime();
    }

    @Override
    public boolean verifyPayment(String externalTransactionId) {
        // 50% de probabilidad de éxito
        boolean success = externalTransactionId.hashCode() % 2 == 0;
        log.info("[MockPaymentAdapter] Mock verification: txId={}, result={}", externalTransactionId, success);
        return success;
    }

    @Override
    public void refundPayment(String externalTransactionId, String reason) {
        log.info("[MockPaymentAdapter] Mock refund: txId={}, reason={}", externalTransactionId, reason);
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.MOCK;
    }
}
