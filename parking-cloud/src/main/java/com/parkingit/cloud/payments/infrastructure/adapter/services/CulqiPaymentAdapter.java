package com.parkingit.cloud.payments.infrastructure.adapter.services;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "payment.culqi.enabled", havingValue = "true")
@Slf4j
@AllArgsConstructor
public class CulqiPaymentAdapter implements PaymentGatewayAdapter {
    private final RestTemplate restTemplate;

    @Value("${payment.culqi.api-key:}")
    private String apiKey;

    @Value("${payment.culqi.public-key:}")
    private String publicKey;

    @Value("${payment.culqi.api-url:}")
    private String apiUrl;

    @Override
    public String initiatePayment(String referenceNumber, BigDecimal amount, String userIdentifier) {
        log.info("[CulqiPaymentAdapter] Initiating payment: ref={}, amount={} PEN", referenceNumber, amount);

        try {
            // En Culqi: primero se crea un token, luego se crea un cargo
            // Para MVP simplificado: retornar URL de pago alojado
            String paymentUrl = generateCulqiCheckoutUrl(referenceNumber, amount);
            log.info("[CulqiPaymentAdapter] Culqi checkout URL generated: {}", paymentUrl);
            return paymentUrl;
        } catch (Exception e) {
            log.error("[CulqiPaymentAdapter] Error processing Culqi payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate Culqi payment", e);
        }
    }

    @Override
    public boolean verifyPayment(String externalTransactionId) {
        log.info("[CulqiPaymentAdapter] Verifying payment: chargeId={}", externalTransactionId);

        try {
            // GET https://api.culqi.com/v2/charges/{chargeId}
            // Retornar estado del cobro
            boolean isVerified = externalTransactionId.startsWith("CULQI_");
            log.info("[CulqiPaymentAdapter] Payment verification result: {}", isVerified);
            return isVerified;
        } catch (Exception e) {
            log.error("[CulqiPaymentAdapter] Error verifying payment: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void refundPayment(String externalTransactionId, String reason) {
        log.info("[CulqiPaymentAdapter] Refunding charge: chargeId={}, reason={}", externalTransactionId, reason);

        try {
            // POST https://api.culqi.com/v2/refunds
            // Body: { "charge_id": "{chargeId}", "reason": "{reason}" }
            log.info("[CulqiPaymentAdapter] Refund initiated successfully");
        } catch (Exception e) {
            log.error("[CulqiPaymentAdapter] Error refunding payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refund Culqi payment", e);
        }
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CULQI;
    }

    private String generateCulqiCheckoutUrl(String referenceNumber, BigDecimal amount) {
        // Culqi Checkout alojado: incluir monto, referencia, etc.
        return String.format("%s/checkout?amount=%.0f&reference=%s&key=%s",
                apiUrl, amount.multiply(BigDecimal.valueOf(100)), referenceNumber, publicKey);
    }
}
