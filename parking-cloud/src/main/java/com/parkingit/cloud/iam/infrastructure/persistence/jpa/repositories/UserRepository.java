package com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.valueobjects.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findUserById(UUID id);
    List<User> findAllByRoles_Name(Roles role);

    Optional<User> findByEmail_Value(String emailValue);

    boolean existsByEmail_Value(String emailValue);
}
