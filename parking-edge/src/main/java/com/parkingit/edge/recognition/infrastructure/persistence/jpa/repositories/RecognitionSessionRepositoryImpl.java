package com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories;

import com.parkingit.edge.recognition.domain.model.entities.RecognitionSession;
import com.parkingit.edge.recognition.domain.model.valueobjects.RecognitionStatus;
import com.parkingit.edge.recognition.infrastructure.persistence.jpa.entities.RecognitionSessionJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecognitionSessionRepositoryImpl implements RecognitionSessionRepository {
    private final RecognitionSessionJpaRepository jpaRepository;

    @Override
    public RecognitionSession save(RecognitionSession session) {
        RecognitionSessionJpaEntity entity = RecognitionSessionJpaEntity.builder()
                .id(session.getId())
                .parkingId(session.getParkingId())
                .driverId(session.getDriverId())
                .status(session.getStatus().name())
                .activatedAt(session.getActivatedAt())
                .deactivatedAt(session.getDeactivatedAt())
                .timeoutAt(session.getTimeoutAt())
                .timedOut(session.getTimedOut())
                .createdAt(LocalDateTime.now())
                .build();

        jpaRepository.save(entity);
        return session;
    }

    @Override
    public Optional<RecognitionSession> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<RecognitionSession> findActiveSessionByParkingId(UUID parkingId, String status, LocalDateTime now) {
        return jpaRepository.findFirstByParkingIdAndStatusAndTimeoutAtAfterOrderByActivatedAtDesc(parkingId, status, now)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteExpiredSessions(String status) {
        var expiredSessions = jpaRepository.findByStatusAndTimeoutAtLessThanEqual(status, LocalDateTime.now());
        jpaRepository.deleteAll(expiredSessions);
    }

    private RecognitionSession toDomain(RecognitionSessionJpaEntity entity) {
        return RecognitionSession.builder()
                .id(entity.getId())
                .parkingId(entity.getParkingId())
                .driverId(entity.getDriverId())
                .status(RecognitionStatus.valueOf(entity.getStatus()))
                .activatedAt(entity.getActivatedAt())
                .deactivatedAt(entity.getDeactivatedAt())
                .timeoutAt(entity.getTimeoutAt())
                .timedOut(entity.getTimedOut())
                .build();
    }
}
