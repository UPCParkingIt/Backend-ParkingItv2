package com.parkingit.cloud.payments.domain.model.aggregates;

import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentAmount;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentMethod;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentStage;
import com.parkingit.cloud.payments.domain.model.valueobjects.PaymentStatus;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PRIVATE)
public class Payment extends AuditableAbstractAggregateRoot<Payment> {
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "parking_log_id")
    private UUID parkingLogId;

    @Embedded
    private PaymentAmount amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_stage", nullable = false)
    private PaymentStage paymentStage;

    @Column(name = "external_transaction_id", unique = true)
    private String externalTransactionId;  // Yape/Plin/Culqi reference

    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    @Column(name = "qr_code_url", columnDefinition = "TEXT")
    private String qrCodeUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "attempted_at")
    private LocalDateTime initiatedAt;

    @Column(name = "driver_paid_at")
    private LocalDateTime driverPaidAt;

    @Column(name = "admin_reviewed_at")
    private LocalDateTime adminReviewedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "admin_reviewer_id")
    private UUID adminReviewerId;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "is_retryable")
    private Boolean isRetryable = true;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    public static Payment createForExit(
            UUID reservationId,
            UUID parkingLogId,
            PaymentAmount amount,
            PaymentMethod paymentMethod,
            String description
    ) {
        Payment payment = new Payment();
        payment.reservationId = reservationId;
        payment.parkingLogId = parkingLogId;
        payment.amount = amount;
        payment.paymentMethod = paymentMethod;
        payment.status = PaymentStatus.PENDING;
        payment.paymentStage = PaymentStage.PENDING_DRIVER_PAYMENT;
        payment.description = description;
        payment.retryCount = 0;
        return payment;
    }

    public void generateQrForDriver(String qrCodeUrl) {
        if (paymentStage != PaymentStage.PENDING_DRIVER_PAYMENT) {
            throw new IllegalStateException("Can only generate QR when pending driver payment");
        }
        this.qrCodeUrl = qrCodeUrl;
        this.status = PaymentStatus.PROCESSING;
    }

    public void markAsDriverPaid() {
        if (!status.equals(PaymentStatus.PROCESSING)) {
            throw new IllegalStateException("Payment must be in PROCESSING state");
        }
        this.status = PaymentStatus.PENDING;
        this.paymentStage = PaymentStage.PENDING_ADMIN_REVIEW;
        this.driverPaidAt = LocalDateTime.now();
    }

    public void approveByAdmin(UUID adminId, String notes) {
        if (paymentStage != PaymentStage.PENDING_ADMIN_REVIEW) {
            throw new IllegalStateException("Payment is not pending admin review");
        }
        this.status = PaymentStatus.COMPLETED;
        this.paymentStage = PaymentStage.APPROVED_BY_ADMIN;
        this.adminReviewerId = adminId;
        this.adminNotes = notes;
        this.adminReviewedAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    public void rejectByAdmin(UUID adminId, String reason) {
        if (paymentStage != PaymentStage.PENDING_ADMIN_REVIEW) {
            throw new IllegalStateException("Payment is not pending admin review");
        }
        this.status = PaymentStatus.FAILED;
        this.paymentStage = PaymentStage.REJECTED_BY_ADMIN;
        this.adminReviewerId = adminId;
        this.adminNotes = reason;
        this.failureReason = reason;
        this.adminReviewedAt = LocalDateTime.now();
        this.isRetryable = true;
    }

    public void allowDriverExit() {
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments allow exit");
        }
        this.paymentStage = PaymentStage.DRIVER_EXITED_ALLOWED;
    }

    public void blockDriverExit() {
        this.paymentStage = PaymentStage.DRIVER_BLOCKED_AT_EXIT;
    }

    public void markAsComplete(String externalTransactionId) {
        this.status = PaymentStatus.COMPLETED;
        this.externalTransactionId = externalTransactionId;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason, boolean retryable) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.isRetryable = retryable;
        if (retryable) {
            this.retryCount++;
        }
    }

    public static Payment createForReservation(
            UUID reservationId,
            PaymentAmount amount,
            PaymentMethod paymentMethod,
            String description
    ) {
        Payment payment = new Payment();
        payment.reservationId = reservationId;
        payment.amount = amount;
        payment.paymentMethod = paymentMethod;
        payment.status = PaymentStatus.PENDING;
        payment.description = description;
        payment.retryCount = 0;
        return payment;
    }

    public void initiate() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already initiated");
        }
        this.status = PaymentStatus.PROCESSING;
        this.initiatedAt = LocalDateTime.now();
        this.referenceNumber = generateReferenceNumber();
    }

    public void complete(String externalTransactionId) {
        if (status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment is not in PROCESSING state");
        }
        this.status = PaymentStatus.COMPLETED;
        this.externalTransactionId = externalTransactionId;
        this.completedAt = LocalDateTime.now();
    }

    public void refund() {
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    public void expire() {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.PROCESSING) {
            this.status = PaymentStatus.EXPIRED;
        }
    }

    public boolean canRetry() {
        return isRetryable && retryCount < 3 && status == PaymentStatus.FAILED;
    }

    private String generateReferenceNumber() {
        return String.format("PO-%tY%tm%td-%05d",
                LocalDateTime.now(),
                getId().hashCode() % 100000);
    }
}
