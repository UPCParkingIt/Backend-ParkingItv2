package com.parkingit.edge.recognition.interfaces.rest.resources;

import java.util.UUID;

public record ActivateRecognitionResource(
        UUID parkingId,
        UUID driverId,
        Integer timeoutSeconds
) {
}
