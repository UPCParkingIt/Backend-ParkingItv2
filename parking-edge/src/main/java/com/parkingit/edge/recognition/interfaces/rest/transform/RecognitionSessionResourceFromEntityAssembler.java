package com.parkingit.edge.recognition.interfaces.rest.transform;

import com.parkingit.edge.recognition.domain.model.entities.RecognitionSession;
import com.parkingit.edge.recognition.interfaces.rest.resources.RecognitionSessionResource;

public class RecognitionSessionResourceFromEntityAssembler {
    public static RecognitionSessionResource toResourceFromEntity(RecognitionSession entity) {
        return new RecognitionSessionResource(
                entity.getId(),
                entity.getParkingId(),
                entity.getDriverId(),
                entity.getStatus().toString(),
                entity.getActivatedAt(),
                entity.getTimeoutAt(),
                entity.getTimedOut()
        );
    }
}
