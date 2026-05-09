package com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories;

import com.parkingit.edge.recognition.domain.model.entities.RecognitionProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecognitionProcessRepository extends JpaRepository<RecognitionProcess, UUID> {
    @Query("SELECT r FROM RecognitionProcess r ORDER BY r.createdAt DESC LIMIT 1")
    Optional<RecognitionProcess> findLatestProcess();
}
