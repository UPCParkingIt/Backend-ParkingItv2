package com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.iam.domain.model.entities.UserCompanion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserCompanionRepository extends JpaRepository<UserCompanion, UUID> {
}
