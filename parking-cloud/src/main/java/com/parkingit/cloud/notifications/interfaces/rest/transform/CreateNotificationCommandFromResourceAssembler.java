package com.parkingit.cloud.notifications.interfaces.rest.transform;

import com.parkingit.cloud.notifications.domain.model.commands.CreateNotificationCommand;
import com.parkingit.cloud.notifications.interfaces.rest.resources.CreateNotificationResource;

public class CreateNotificationCommandFromResourceAssembler {
    public static CreateNotificationCommand toCommandFromResource(CreateNotificationResource resource) {
        return new CreateNotificationCommand(
                resource.recipientUserId(),
                resource.recipientEmail(),
                resource.subject(),
                resource.messageBody(),
                resource.attachmentPath(),
                resource.sendEmail()
        );
    }
}
