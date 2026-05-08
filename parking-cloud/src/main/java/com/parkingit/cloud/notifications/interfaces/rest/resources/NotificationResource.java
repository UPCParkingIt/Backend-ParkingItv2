package com.parkingit.cloud.notifications.interfaces.rest.resources;

import java.util.UUID;

public record NotificationResource(
        UUID id,
        UUID recipientUserId,
        String subject,
        String messageBody
) {
}
