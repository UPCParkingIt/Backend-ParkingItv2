package com.parkingit.edge.recognition.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecognitionSessionResource(
        UUID sessionId,
        UUID parkingId,
        UUID driverId,
        String status,
        LocalDateTime activatedAt,
        LocalDateTime timeoutAt,
        Boolean timedOut
) {
}
