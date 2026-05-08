//package com.parkingit.cloud.payments.infrastructure.adapter.services;
//
//import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
//import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayAdapter;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//
//@Service
//@AllArgsConstructor
//@Slf4j
//public class YapePaymentAdapter implements PaymentGatewayAdapter {
//    @Value("${payment.yape.merchant-phone}")
//    private String merchantPhone;
//
//    @Value("${payment.yape.merchant-name:ParkingIT}")
//    private String merchantName;
//
//    @Override
//    public String initiatePayment(String referenceNumber, BigDecimal amount, String userIdentifier) {
//        log.info("[YapePaymentAdapter] Initiating payment: ref={}, amount={} PEN", referenceNumber, amount);
//
//        try {
//            // Generar QR dinámico: Yape utiliza formato standardizado
//            // Formato: https://yape.pe/pay?amount={monto}&reference={ref}&merchant={nombre}
//            String qrUrl = generateYapeQrUrl(referenceNumber, amount);
//            log.info("[YapePaymentAdapter] QR URL generated: {}", qrUrl);
//            return qrUrl;
//        } catch (Exception e) {
//            log.error("[YapePaymentAdapter] Error generating QR: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to generate Yape QR", e);
//        }
//    }
//
//    @Override
//    public boolean verifyPayment(String externalTransactionId) {
//        log.info("[YapePaymentAdapter] Verifying payment: txId={}", externalTransactionId);
//
//        // Yape requiere:
//        // 1. Polling manual cada 5-10 segundos
//        // 2. O webhook configurado en dashboard de Yape
//        // Para MVP: simulamos verificación (debe implementarse con webhook real)
//
//        boolean isVerified = externalTransactionId.startsWith("YAPE_");
//        log.info("[YapePaymentAdapter] Payment verification result: {}", isVerified);
//        return isVerified;
//    }
//
//    @Override
//    public void refundPayment(String externalTransactionId, String reason) {
//        log.warn("[YapePaymentAdapter] Refund requested: txId={}, reason={}", externalTransactionId, reason);
//
//        // Yape no soporta reembolsos automáticos
//        // Contactar soporte: support@yape.com.pe
//        log.warn("[YapePaymentAdapter] Manual refund required - contact Yape support");
//    }
//
//    @Override
//    public PaymentMethod getMethod() {
//        return PaymentMethod.YAPE;
//    }
//
//    private String generateYapeQrUrl(String referenceNumber, BigDecimal amount) throws Exception {
//        String encodedRef = URLEncoder.encode(referenceNumber, StandardCharsets.UTF_8);
//        String encodedMerchant = URLEncoder.encode(merchantName, StandardCharsets.UTF_8);
//
//        return String.format("https://yape.pe/pay?amount=%.2f&reference=%s&merchant=%s&phone=%s",
//                amount, encodedRef, encodedMerchant, merchantPhone);
//    }
//}
