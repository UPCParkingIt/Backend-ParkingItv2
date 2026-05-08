package com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.iam.domain.model.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserIdAndIsUsedTrue(UUID userId);
}
