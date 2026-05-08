package com.parkingit.cloud.notifications.application.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.notifications.domain.model.commands.CreateNotificationCommand;
import com.parkingit.cloud.notifications.domain.services.NotificationCommandService;
import com.parkingit.cloud.notifications.interfaces.acl.NotificationContextFacade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationContextFacadeImpl implements NotificationContextFacade {
    private final NotificationCommandService commandService;

    @Override
    public void createNotification(User recipientUser, String subject, String messageBody, String attachmentPath, Boolean sendEmail) {
        var notificationResult = commandService.handle(
                new CreateNotificationCommand(
                        recipientUser.getId(),
                        recipientUser.getEmail().getValue(),
                        subject,
                        messageBody,
                        attachmentPath,
                        sendEmail
                )
        );

        if (notificationResult.isEmpty()) {
            throw new IllegalArgumentException("Failed to create notification for user: " + recipientUser);
        }

    }
}
