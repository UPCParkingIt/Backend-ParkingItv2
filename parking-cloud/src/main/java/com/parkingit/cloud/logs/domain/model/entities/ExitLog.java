package com.parkingit.cloud.logs.domain.model.entities;

import com.parkingit.cloud.logs.domain.model.valueobjects.VerificationResult;
import com.parkingit.shared.domain.valueobjects.FacialEmbedding;
import com.parkingit.shared.domain.valueobjects.LicensePlate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "exit_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PRIVATE)
public class ExitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Embedded
    @AttributeOverride(name = "plate", column = @Column(name = "vehicle_plate"))
    private LicensePlate licensePlate;

    @Embedded
    @AttributeOverride(name = "embeddingVector", column = @Column(name = "facial_embedding", columnDefinition = "TEXT"))
    private FacialEmbedding facialEmbedding;

    @Embedded
    private VerificationResult verificationResult;

    @Column(name = "exit_timestamp", nullable = false)
    private LocalDateTime exitTimestamp;

    @Column(name = "parking_id", nullable = false)
    private UUID parkingId;

    @Column(name = "entry_log_id", nullable = false)
    private UUID entryLogId;

    public static ExitLog create(
            LicensePlate licensePlate,
            FacialEmbedding facialEmbedding,
            VerificationResult verificationResult,
            UUID parkingId,
            UUID entryLogId
    ) {
        ExitLog exitLog = new ExitLog();
        exitLog.licensePlate = licensePlate;
        exitLog.facialEmbedding = facialEmbedding;
        exitLog.verificationResult = verificationResult;
        exitLog.exitTimestamp = LocalDateTime.now();
        exitLog.parkingId = parkingId;
        exitLog.entryLogId = entryLogId;
        return exitLog;
    }
}
