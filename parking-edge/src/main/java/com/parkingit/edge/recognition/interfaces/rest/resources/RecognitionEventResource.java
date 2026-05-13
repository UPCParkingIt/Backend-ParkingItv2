package com.parkingit.edge.recognition.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RecognitionEventResource(
        @NotBlank String type, // "PLATE" or "FACE"
        @NotBlank String value, // License plate number or Facial ID
        UUID parkingId // Optional, can be injected by the edge service if not provided
) {
}
