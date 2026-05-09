package com.parkingit.edge.recognition.domain.services;

import com.parkingit.edge.recognition.domain.model.commands.ActivateRecognition;
import com.parkingit.edge.recognition.domain.model.commands.ActivateRecognitionCommand;
import com.parkingit.edge.recognition.domain.model.commands.DeactivateRecognitionCommand;
import com.parkingit.edge.recognition.domain.model.entities.RecognitionSession;
import com.parkingit.edge.recognition.domain.model.queries.GetLatestStatusQuery;

import java.util.Optional;
import java.util.UUID;

public interface RecognitionManagementService {
    RecognitionSession handle(ActivateRecognitionCommand command);
    void handle(DeactivateRecognitionCommand command);
    Optional<RecognitionSession> getActiveSession(UUID parkingId);
    Optional<RecognitionSession> getSessionById(UUID sessionId);

    Boolean handle(ActivateRecognition command);

    Boolean handle(GetLatestStatusQuery command);
}
