package com.parkingit.edge.recognition.domain.model.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecognitionDeactivatedEvent {
    private UUID sessionId;
    private UUID parkingId;
    private UUID driverId;
    private LocalDateTime deactivatedAt;
    private Boolean timedOut;
    private String reason;
}
