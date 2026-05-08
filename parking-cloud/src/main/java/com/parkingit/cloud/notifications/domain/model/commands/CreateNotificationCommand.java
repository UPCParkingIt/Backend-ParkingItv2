package com.parkingit.cloud.notifications.domain.model.commands;

import java.util.UUID;

public record CreateNotificationCommand(
        UUID recipientUserId,
        String recipientEmail,
        String subject,
        String messageBody,
        String attachmentPath,
        Boolean sendEmail
) {
}
