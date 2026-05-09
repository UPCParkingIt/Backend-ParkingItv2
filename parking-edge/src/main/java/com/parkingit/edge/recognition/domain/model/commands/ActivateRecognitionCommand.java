package com.parkingit.edge.recognition.domain.model.commands;

import java.util.UUID;

public record ActivateRecognitionCommand(
        UUID parkingId,
        UUID driverId,
        Integer timeoutSeconds
) {
}
