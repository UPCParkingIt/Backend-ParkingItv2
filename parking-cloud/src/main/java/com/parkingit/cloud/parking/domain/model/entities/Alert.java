package com.parkingit.cloud.parking.domain.model.entities;

import com.parkingit.cloud.parking.domain.model.valueobjects.AlertSeverity;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertStatus;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parking_alerts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "parking_id", nullable = false)
    private UUID parkingId;

    @Column(name = "parking_log_id")
    private UUID parkingLogId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status = AlertStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    public static Alert create(UUID parkingId, AlertType type, AlertSeverity severity, String description) {
        Alert alert = new Alert();
        alert.parkingId = parkingId;
        alert.alertType = type;
        alert.severity = severity;
        alert.description = description;
        alert.status = AlertStatus.PENDING;
        alert.createdAt = LocalDateTime.now();
        return alert;
    }

    public void review(String notes) {
        this.status = AlertStatus.REVIEWED;
        this.reviewedAt = LocalDateTime.now();
        this.reviewerNotes = notes;
    }

    public void resolve(String notes) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.reviewerNotes = notes;
    }

    public void markAsFalseAlarm(String notes) {
        this.status = AlertStatus.FALSE_ALARM;
        this.resolvedAt = LocalDateTime.now();
        this.reviewerNotes = notes;
    }

    public long getAgeInMinutes() {
        return java.time.temporal.ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
    }

    public boolean isUrgent() {
        return status == AlertStatus.PENDING && getAgeInMinutes() > 30;
    }
}
