package com.parkingit.edge.recognition.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recognition_sessions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionSessionJpaEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, columnDefinition = "UUID")
    private UUID parkingId;

    @Column(nullable = false, columnDefinition = "UUID")
    private UUID driverId;

    @Column(nullable = false)
    private String status;  // INACTIVE, ACTIVE, SCANNING, DETECTED

    @Column(nullable = false)
    private LocalDateTime activatedAt;

    @Column
    private LocalDateTime deactivatedAt;

    @Column(nullable = false)
    private LocalDateTime timeoutAt;

    @Column
    private String accessCode;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean timedOut;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
}
