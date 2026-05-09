package com.parkingit.edge.recognition.application.internal.inboundservices.schedule;

import com.parkingit.edge.recognition.domain.model.valueobjects.RecognitionStatus;
import com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories.RecognitionSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecognitionSessionCleanUpService {
    private final RecognitionSessionRepository sessionRepository;

    @Scheduled(fixedDelayString = "${edge.recognition.cleanup-interval:}")
    public void cleanupExpiredSessions() {
        try {
            log.debug("Running recognition session cleanup job");
            sessionRepository.deleteExpiredSessions(RecognitionStatus.ACTIVE.toString());
        } catch (Exception e) {
            log.error("Error cleaning up expired LPR sessions", e);
        }
    }
}
