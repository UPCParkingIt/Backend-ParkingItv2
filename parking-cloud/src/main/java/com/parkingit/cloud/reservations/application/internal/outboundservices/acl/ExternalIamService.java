package com.parkingit.cloud.reservations.application.internal.outboundservices.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.interfaces.acl.IamContextFacade;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("reservationIamService")
public class ExternalIamService {
    @Lazy private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<User> fetchUserById(UUID userId) {
        return iamContextFacade.fetchUserById(userId);
    }
}
