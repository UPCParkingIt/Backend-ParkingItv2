package com.parkingit.cloud.iam.interfaces.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;

import java.util.Optional;
import java.util.UUID;

public interface IamContextFacade {
    Optional<User> fetchUserById(UUID userId);
    UUID fetchUserIdByEmail(String email);
}
