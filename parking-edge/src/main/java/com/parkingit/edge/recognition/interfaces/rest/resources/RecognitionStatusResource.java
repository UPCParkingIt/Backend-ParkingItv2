package com.parkingit.edge.recognition.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecognitionStatusResource(
        UUID sessionId,
        UUID parkingId,
        String status,
        Boolean timedOut,
        LocalDateTime activatedAt,
        LocalDateTime timeoutAt
) {
}
