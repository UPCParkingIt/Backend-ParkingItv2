package com.parkingit.edge.recognition.interfaces.rest.resources;

import java.util.UUID;

public record DeactivateRecognitionResource(
        UUID parkingId,
        UUID sessionId
) {
}
