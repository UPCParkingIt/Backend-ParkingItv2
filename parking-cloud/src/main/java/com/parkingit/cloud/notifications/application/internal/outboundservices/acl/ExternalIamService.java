package com.parkingit.cloud.notifications.application.internal.outboundservices.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.interfaces.acl.IamContextFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("notificationsIamService")
public class ExternalIamService {
    @Lazy private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<User> fetchUserById(UUID userId) {
        return iamContextFacade.fetchUserById(userId);
    }

    public Optional<User> fetchUserByEmail(String email) {
        UUID userId = iamContextFacade.fetchUserIdByEmail(email);
        if (userId == null) return Optional.empty();
        return iamContextFacade.fetchUserById(userId);
    }

    public UUID fetchUserIdByEmail(String email) {
        return iamContextFacade.fetchUserIdByEmail(email);
    }
}
