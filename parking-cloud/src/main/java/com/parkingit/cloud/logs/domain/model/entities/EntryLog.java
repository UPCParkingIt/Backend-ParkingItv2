package com.parkingit.cloud.logs.domain.model.entities;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PRIVATE)
public class EntryLog {
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

    @Column(name = "entry_timestamp", nullable = false)
    private LocalDateTime entryTimestamp;

    @Column(name = "parking_id", nullable = false)
    private UUID parkingId;

    @Column(name = "user_id")
    private UUID userId;

    public static EntryLog create(
            LicensePlate licensePlate,
            FacialEmbedding facialEmbedding,
            UUID parkingId,
            UUID userId
    ) {
        EntryLog entryLog = new EntryLog();
        entryLog.licensePlate = licensePlate;
        entryLog.facialEmbedding = facialEmbedding;
        entryLog.entryTimestamp = LocalDateTime.now();
        entryLog.parkingId = parkingId;
        entryLog.userId = userId;
        return entryLog;
    }
}
