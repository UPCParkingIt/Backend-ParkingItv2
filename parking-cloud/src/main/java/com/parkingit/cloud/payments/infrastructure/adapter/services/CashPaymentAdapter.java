package com.parkingit.cloud.payments.infrastructure.adapter.services;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "payment.cash.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CashPaymentAdapter implements PaymentGatewayAdapter {
    @Override
    public String initiatePayment(String referenceNumber, BigDecimal amount, String userIdentifier) {
        log.info("[CashPaymentAdapter] Cash payment initiated: ref={}, amount={} PEN", referenceNumber, amount);

        String receipt = generateCashReceipt(referenceNumber, amount);
        log.info("[CashPaymentAdapter] Receipt generated: {}", receipt);
        return receipt;
    }

    @Override
    public boolean verifyPayment(String externalTransactionId) {
        log.info("[CashPaymentAdapter] Verifying cash payment: receiptId={}", externalTransactionId);

        // El operador debe confirmar manualmente
        // Por ahora: Siempre retornar false (requiere confirmación manual)
        return false;
    }

    @Override
    public void refundPayment(String externalTransactionId, String reason) {
        log.info("[CashPaymentAdapter] Cash refund: receiptId={}, reason={}", externalTransactionId, reason);
        log.info("[CashPaymentAdapter] Operario debe procesar reembolso en caja");
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CASH;
    }

    private String generateCashReceipt(String referenceNumber, BigDecimal amount) {
        return String.format("CASH_%d", System.currentTimeMillis());
    }
}
