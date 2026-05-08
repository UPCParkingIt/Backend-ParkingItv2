package com.parkingit.cloud.payments.infrastructure.adapter.services;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "payment.plin.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class PlinPaymentAdapter implements PaymentGatewayAdapter {
    @Value("${payment.plin.merchant-id:}")
    private String merchantId;

    @Value("${payment.plin.merchant-name:ParkingIT}")
    private String merchantName;

    @Override
    public String initiatePayment(String referenceNumber, BigDecimal amount, String userIdentifier) {
        log.info("[PlinPaymentAdapter] Initiating payment: ref={}, amount={} PEN", referenceNumber, amount);

        try {
            String qrUrl = generatePlinQrUrl(referenceNumber, amount);
            log.info("[PlinPaymentAdapter] Plin payment link generated: {}", qrUrl);
            return qrUrl;
        } catch (Exception e) {
            log.error("[PlinPaymentAdapter] Error generating Plin link: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Plin payment link", e);
        }
    }

    @Override
    public boolean verifyPayment(String externalTransactionId) {
        log.info("[PlinPaymentAdapter] Verifying payment: txId={}", externalTransactionId);

        // Similar a Yape: requiere webhook o polling
        boolean isVerified = externalTransactionId.startsWith("PLIN_");
        log.info("[PlinPaymentAdapter] Payment verification result: {}", isVerified);
        return isVerified;
    }

    @Override
    public void refundPayment(String externalTransactionId, String reason) {
        log.warn("[PlinPaymentAdapter] Refund requested: txId={}, reason={}", externalTransactionId, reason);
        log.warn("[PlinPaymentAdapter] Manual refund required - contact Plin support");
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.PLIN;
    }

    private String generatePlinQrUrl(String referenceNumber, BigDecimal amount) throws Exception {
        String encodedRef = URLEncoder.encode(referenceNumber, StandardCharsets.UTF_8);
        String encodedMerchant = URLEncoder.encode(merchantName, StandardCharsets.UTF_8);

        return String.format("https://plin.pe/payments/%s?amount=%.2f&reference=%s&merchant=%s",
                merchantId, amount, encodedRef, encodedMerchant);
    }
}
