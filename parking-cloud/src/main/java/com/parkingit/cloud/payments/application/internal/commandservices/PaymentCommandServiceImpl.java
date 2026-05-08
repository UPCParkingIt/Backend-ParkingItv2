package com.parkingit.cloud.payments.application.internal.commandservices;

import com.parkingit.cloud.payments.domain.model.aggregates.Payment;
import com.parkingit.cloud.payments.domain.model.commands.*;
import com.parkingit.cloud.payments.domain.model.events.*;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentAmount;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentStatus;
import com.parkingit.cloud.payments.domain.services.PaymentCommandService;
import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayAdapter;
import com.parkingit.cloud.payments.infrastructure.adapter.PaymentGatewayFactory;
import com.parkingit.cloud.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentCommandServiceImpl implements PaymentCommandService {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Payment> handle(InitiatePaymentCommand command) {
        try {
            log.info("[PaymentCommandService] Initiating payment: reservationId={}, amount={}, method={}", command.reservationId(), command.amount(), command.paymentMethod());

            PaymentAmount paymentAmount = PaymentAmount.create(command.amount());
            Payment payment = Payment.createForReservation(
                    command.reservationId(),
                    paymentAmount,
                    command.paymentMethod(),
                    command.description()
            );

            payment.initiate();

            // Obtener adapter del proveedor de pago
            PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapter(command.paymentMethod());

            // Iniciar pago en el proveedor
            String externalTxId = adapter.initiatePayment(
                    payment.getReferenceNumber(),
                    command.amount(),
                    "user_" + command.reservationId()
            );

            log.info("[PaymentCommandService] External transaction initiated: ref={}, externalTxId={}",
                    payment.getReferenceNumber(), externalTxId);

            // Guardar payment en BD
            payment = paymentRepository.save(payment);

            // Publicar evento
            eventPublisher.publishEvent(new PaymentInitiatedEvent(
                    payment.getId(),
                    payment.getReservationId(),
                    payment.getAmount().getAmount(),
                    payment.getPaymentMethod(),
                    payment.getReferenceNumber(),
                    java.time.Instant.now()
            ));

            log.info("[PaymentCommandService] Payment initiated successfully: paymentId={}", payment.getId());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error initiating payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(CompletePaymentCommand command) {
        try {
            log.info("[PaymentCommandService] Completing payment: paymentId={}, externalTxId={}",
                    command.paymentId(), command.externalTransactionId());

            Payment payment = paymentRepository.findById(command.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + command.paymentId()));

            if (payment.getStatus() != PaymentStatus.PROCESSING) {
                throw new IllegalStateException("Payment is not in PROCESSING state");
            }

            payment.complete(command.externalTransactionId());
            payment = paymentRepository.save(payment);

            // Publicar evento
            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    payment.getId(),
                    payment.getReservationId(),
                    command.externalTransactionId(),
                    payment.getAmount().getAmount(),
                    java.time.Instant.now()
            ));

            log.info("[PaymentCommandService] Payment completed successfully: paymentId={}", payment.getId());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error completing payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to complete payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(RefundPaymentCommand command) {
        try {
            log.info("[PaymentCommandService] Refunding payment: paymentId={}, reason={}",
                    command.paymentId(), command.reason());

            Payment payment = paymentRepository.findById(command.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + command.paymentId()));

            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                throw new IllegalStateException("Only completed payments can be refunded");
            }

            // Obtener adapter y procesar reembolso
            PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapter(payment.getPaymentMethod());
            adapter.refundPayment(payment.getExternalTransactionId(), command.reason());

            payment.refund();
            payment = paymentRepository.save(payment);

            log.info("[PaymentCommandService] Payment refunded successfully: paymentId={}", payment.getId());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error refunding payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refund payment: " + e.getMessage(), e);
        }
    }

    @Override
    public void handleFailedPayment(String paymentId, String failureReason) {
        try {
            Payment payment = paymentRepository.findById(java.util.UUID.fromString(paymentId))
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

            boolean retryable = payment.getRetryCount() < 3;
            payment.fail(failureReason, retryable);
            paymentRepository.save(payment);

            //eventPublisher.publishEvent(new PaymentFailedEvent(
            //        payment.getId(),
            //        payment.getReservationId(),
            //        failureReason,
            //        retryable,
            //        java.time.Instant.now()
            //));

            log.warn("[PaymentCommandService] Payment failed: paymentId={}, reason={}, retryable={}",
                    paymentId, failureReason, retryable);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error handling failed payment: {}", e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(InitiateExitPaymentCommand command) {
        try {
            log.info("[PaymentCommandService] Initiating EXIT payment: reservationId={}, amount={}, method={}", command.reservationId(), command.amount(), command.paymentMethod());

            PaymentAmount paymentAmount = PaymentAmount.create(command.amount());
            Payment payment = Payment.createForExit(
                    command.reservationId(),
                    command.parkingLogId(),
                    paymentAmount,
                    command.paymentMethod(),
                    command.description()
            );

            if (command.paymentMethod() == PaymentMethod.YAPE) {
                PaymentGatewayAdapter adapter = paymentGatewayFactory.getAdapter(PaymentMethod.YAPE);
                String qrUrl = adapter.initiatePayment(
                        null,
                        command.amount(),
                        "driver_exit_" + command.reservationId()
                );
                payment.generateQrForDriver(qrUrl);
            } else if (command.paymentMethod() == PaymentMethod.CASH) {
                payment.generateQrForDriver("[CASH_PAYMENT_PENDING]");
            }

            payment = paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentInitiatedEvent(
                    payment.getId(),
                    payment.getReservationId(),
                    payment.getAmount().getAmount(),
                    payment.getPaymentMethod(),
                    payment.getReferenceNumber(),
                    java.time.Instant.now()
            ));

            log.info("[PaymentCommandService] EXIT payment initiated: paymentId={}, qrUrl={}",
                    payment.getId(), payment.getQrCodeUrl());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error initiating EXIT payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate EXIT payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(ApprovePaymentCommand command) {
        try {
            log.info("[PaymentCommandService] Approving payment: paymentId={}, adminId={}", command.paymentId(), command.adminUserId());

            Payment payment = paymentRepository.findById(command.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            payment.approveByAdmin(command.adminUserId(), command.notes());
            payment = paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentApprovedByAdminEvent(
                    payment.getId(),
                    payment.getReservationId(),
                    command.adminUserId(),
                    command.notes(),
                    java.time.Instant.now()
            ));

            log.info("[PaymentCommandService] Payment APPROVED by admin: paymentId={}", payment.getId());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error approving payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to approve payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(RejectPaymentCommand command) {
        try {
            log.info("[PaymentCommandService] Rejecting payment: paymentId={}, adminId={}",
                    command.paymentId(), command.adminUserId());

            Payment payment = paymentRepository.findById(command.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            payment.rejectByAdmin(command.adminUserId(), command.reason());
            payment = paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentRejectedByAdminEvent(
                    payment.getId(),
                    payment.getReservationId(),
                    null,  // TODO: obtener parkingId
                    command.adminUserId(),
                    command.reason(),
                    java.time.Instant.now()
            ));

            log.info("[PaymentCommandService] Payment REJECTED by admin: paymentId={}, reason={}",
                    payment.getId(), command.reason());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error rejecting payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reject payment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(MarkDriverPaidCommand command) {
        try {
            log.info("[PaymentCommandService] Marking payment as driver paid: paymentId={}", command.paymentId());

            Payment payment = paymentRepository.findById(command.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            payment.markAsDriverPaid();
            payment = paymentRepository.save(payment);

            eventPublisher.publishEvent(new PaymentDriverPaidEvent(
                    payment.getId(),
                    payment.getReservationId(),
                    payment.getReferenceNumber(),
                    java.time.Instant.now()
            ));

            log.info("[PaymentCommandService] Payment marked as driver paid - awaiting admin review: paymentId={}",
                    payment.getId());
            return Optional.of(payment);

        } catch (Exception e) {
            log.error("[PaymentCommandService] Error marking driver paid: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark driver paid: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Payment> handle(AllowDriverExitCommand command) {
        return Optional.empty();
    }
}
