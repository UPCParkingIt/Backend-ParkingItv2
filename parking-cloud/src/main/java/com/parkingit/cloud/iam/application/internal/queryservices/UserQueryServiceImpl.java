package com.parkingit.cloud.iam.application.internal.queryservices;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.queries.GetAllUsersByRoleQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetAllUsersQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByEmailQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByIdQuery;
import com.parkingit.cloud.iam.domain.services.UserQueryService;
import com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findUserById(query.userId());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail_Value(query.email());
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    @Override
    public List<User> handle(GetAllUsersByRoleQuery query) {
        return userRepository.findAllByRoles_Name(query.role());
    }
}
