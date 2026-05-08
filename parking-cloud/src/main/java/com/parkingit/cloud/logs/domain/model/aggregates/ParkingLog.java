package com.parkingit.cloud.logs.domain.model.aggregates;

import com.parkingit.cloud.logs.domain.model.entities.EntryLog;
import com.parkingit.cloud.logs.domain.model.entities.ExitLog;
import com.parkingit.cloud.logs.domain.model.valueobjects.LogStatus;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import com.parkingit.shared.domain.valueobjects.LicensePlate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PRIVATE)
public class ParkingLog extends AuditableAbstractAggregateRoot<ParkingLog> {
    @Embedded
    @AttributeOverride(name = "plate", column = @Column(name = "vehicle_plate"))
    private LicensePlate licensePlate;

    @Column(name = "parking_id", nullable = false)
    private UUID parkingId;

    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_log_id")
    private EntryLog entryLog;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "exit_log_id")
    private ExitLog exitLog;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LogStatus status = LogStatus.ENTRY;

    @Column(name = "occupancy_duration_minutes")
    private Long occupancyDurationMinutes;

    @Column(name = "is_alert_generated", nullable = false)
    private Boolean isAlertGenerated = false;

    @Column(name = "alert_reason", columnDefinition = "TEXT")
    private String alertReason;

    public static ParkingLog createFromEntry(LicensePlate licensePlate, EntryLog entryLog, UUID parkingId, UUID userId) {
        ParkingLog log = new ParkingLog();
        log.licensePlate = licensePlate;
        log.parkingId = parkingId;
        log.userId = userId;
        log.entryLog = entryLog;
        log.status = LogStatus.ENTRY;
        log.isAlertGenerated = false;
        return log;
    }

    public void recordExit(ExitLog exitLog) {
        this.exitLog = exitLog;

        if (Boolean.TRUE.equals(exitLog.getVerificationResult().getIsMatched())) {
            this.status = LogStatus.MATCHED;
        } else {
            this.status = LogStatus.MISMATCH;
            this.isAlertGenerated = true;
            this.alertReason = "Facial mismatch detected during exit verification";
        }

        if (this.entryLog != null && this.exitLog != null) {
            Duration duration = Duration.between(
                    this.entryLog.getEntryTimestamp(),
                    this.exitLog.getExitTimestamp()
            );
            this.occupancyDurationMinutes = duration.toMinutes();
        }
    }

    public void markAsAlert(String reason) {
        this.status = LogStatus.ALERT;
        this.isAlertGenerated = true;
        this.alertReason = reason;
    }

    public boolean isExitVerified() {
        return exitLog != null && Boolean.TRUE.equals(exitLog.getVerificationResult().getIsMatched());
    }

    public long calculateOccupancyHours() {
        if (occupancyDurationMinutes == null) return 0;
        return occupancyDurationMinutes / 60;
    }
}
