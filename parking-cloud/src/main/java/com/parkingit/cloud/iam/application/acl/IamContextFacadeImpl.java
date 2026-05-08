package com.parkingit.cloud.iam.application.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByEmailQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByIdQuery;
import com.parkingit.cloud.iam.domain.services.UserCommandService;
import com.parkingit.cloud.iam.domain.services.UserQueryService;
import com.parkingit.cloud.iam.interfaces.acl.IamContextFacade;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class IamContextFacadeImpl implements IamContextFacade {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    @Override
    public Optional<User> fetchUserById(UUID userId) {
        return userQueryService.handle(new GetUserByIdQuery(userId));
    }

    @Override
    public UUID fetchUserIdByEmail(String email) {
        var getUserByEmailQuery = new GetUserByEmailQuery(email);
        var result = userQueryService.handle(getUserByEmailQuery);
        return result.map(AuditableAbstractAggregateRoot::getId).orElse(null);
    }
}
