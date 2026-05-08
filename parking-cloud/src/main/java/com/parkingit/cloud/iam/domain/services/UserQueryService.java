package com.parkingit.cloud.iam.domain.services;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.queries.GetAllUsersByRoleQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetAllUsersQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByEmailQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByEmailQuery query);

    List<User> handle(GetAllUsersQuery query);
    List<User> handle(GetAllUsersByRoleQuery query);
}
