package com.parkingit.cloud.notifications.interfaces.rest.transform;

import com.parkingit.cloud.notifications.domain.model.aggregates.Notification;
import com.parkingit.cloud.notifications.interfaces.rest.resources.NotificationResource;

public class NotificationResourceFromEntityAssembler {
    public static NotificationResource toResourceFromEntity(Notification entity) {
        return new NotificationResource(
                entity.getId(),
                entity.getRecipientUserId(),
                entity.getSubject(),
                entity.getMessageBody()
        );
    }
}
