package com.parkingit.cloud.notifications.interfaces.rest.resources;

import java.util.UUID;

public record CreateNotificationResource(
        UUID recipientUserId,
        String recipientEmail,
        String subject,
        String messageBody,
        String attachmentPath,
        Boolean sendEmail
) {
}
