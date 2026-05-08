package com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.logs.domain.model.entities.ExitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExitLogRepository extends JpaRepository<ExitLog, UUID> {
}
