package com.parkingit.cloud.iam.application.internal.eventhandlers;

import com.parkingit.cloud.iam.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.iam.domain.model.events.UserCreatedEvent;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByIdQuery;
import com.parkingit.cloud.iam.domain.services.UserQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserCreationEventHandler {
    private final UserQueryService userQueryService;
    private final ExternalNotificationService externalNotificationService;

    @EventListener(UserCreatedEvent.class)
    public void on(UserCreatedEvent event) {
        try {
            var user = userQueryService.handle(new GetUserByIdQuery(event.getUserId()));

            if (user.isEmpty()) {
                throw new RuntimeException("User not found with ID: " + event.getUserId());
            }

            var userEntity = user.get();

            //if (userEntity.getRoles().stream().anyMatch(role -> role.getName() == Roles.PASSENGER_ROLE)) {
            //    externalNotificationService.createNotification(
            //            userEntity.getId(),
            //            "Welcome to GoUni!",
            //            "Welcome aboard! You can now start using the platform."
            //    );
            //} else if (userEntity.getRoles().stream().anyMatch(role -> role.getName() == Roles.DRIVER_ROLE)) {
            //    externalNotificationService.createNotification(
            //            userEntity.getId(),
            //            "Welcome Driver to GoUni!",
            //            "Welcome aboard Driver! You can now start managing the platform."
            //    );
            //}
        } catch (Exception e) {
            log.error("[UserCreationEventHandler] Error handling user creation event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
