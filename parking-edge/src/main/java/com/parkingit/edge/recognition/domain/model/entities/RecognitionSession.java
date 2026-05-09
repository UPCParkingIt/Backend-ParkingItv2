package com.parkingit.edge.recognition.domain.model.entities;

import com.parkingit.edge.recognition.domain.model.valueobjects.RecognitionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionSession {
    private UUID id;
    private UUID parkingId;
    private UUID driverId;
    private RecognitionStatus status;
    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;
    private LocalDateTime timeoutAt;
    private Boolean timedOut;

    public static RecognitionSession createActive(
            UUID parkingId,
            UUID driverId,
            int timeoutSeconds
    ) {
        LocalDateTime now = LocalDateTime.now();
        return RecognitionSession.builder()
                .id(UUID.randomUUID())
                .parkingId(parkingId)
                .driverId(driverId)
                .status(RecognitionStatus.ACTIVE)
                .activatedAt(now)
                .timeoutAt(now.plusSeconds(timeoutSeconds))
                .timedOut(false)
                .build();
    }

    public void detectLicensePlate(String licensePlate) {
        if (status != RecognitionStatus.ACTIVE) {
            throw new IllegalStateException("Recognition session not active. Current status: " + status);
        }
        if (isExpired()) {
            throw new IllegalStateException("Recognition session has expired");
        }
        this.status = RecognitionStatus.DETECTED;
    }

    public void deactivate() {
        this.status = RecognitionStatus.INACTIVE;
        this.deactivatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        if (timeoutAt == null) return false;
        return LocalDateTime.now().isAfter(timeoutAt);
    }

    public void autoDeactivateIfExpired() {
        if (isExpired() && status != RecognitionStatus.INACTIVE) {
            this.status = RecognitionStatus.INACTIVE;
            this.timedOut = true;
            this.deactivatedAt = LocalDateTime.now();
        }
    }

    public boolean isActive() {
        return status == RecognitionStatus.ACTIVE && !isExpired();
    }
}
