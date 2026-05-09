package com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories;

import com.parkingit.edge.recognition.infrastructure.persistence.jpa.entities.RecognitionSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecognitionSessionJpaRepository extends JpaRepository<RecognitionSessionJpaEntity, UUID> {
    Optional<RecognitionSessionJpaEntity> findFirstByParkingIdAndStatusAndTimeoutAtAfterOrderByActivatedAtDesc(
            UUID parkingId,
            String status,
            LocalDateTime now
    );

    List<RecognitionSessionJpaEntity> findByStatusAndTimeoutAtLessThanEqual(
            String status,
            LocalDateTime now
    );

    List<RecognitionSessionJpaEntity> findByParkingIdOrderByActivatedAtDesc(UUID parkingId);
}
