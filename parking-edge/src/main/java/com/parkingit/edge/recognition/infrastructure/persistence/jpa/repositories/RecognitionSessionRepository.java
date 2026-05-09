package com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories;

import com.parkingit.edge.recognition.domain.model.entities.RecognitionSession;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RecognitionSessionRepository {
    RecognitionSession save(RecognitionSession session);
    Optional<RecognitionSession> findById(UUID id);
    Optional<RecognitionSession> findActiveSessionByParkingId(UUID parkingId, String status, LocalDateTime date);
    void deleteById(UUID id);
    void deleteExpiredSessions(String status);
}
