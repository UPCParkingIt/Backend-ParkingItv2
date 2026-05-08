package com.parkingit.cloud.notifications.application.internal.commandservices;

import com.parkingit.cloud.notifications.domain.model.aggregates.Notification;
import com.parkingit.cloud.notifications.domain.model.commands.CreateNotificationCommand;
import com.parkingit.cloud.notifications.domain.services.EmailService;
import com.parkingit.cloud.notifications.domain.services.NotificationCommandService;
import com.parkingit.cloud.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationCommandServiceImpl implements NotificationCommandService {
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public Optional<Notification> handle(CreateNotificationCommand command) {
        try {
            log.info("[NotificationCommandService] Creating notification for user ID: {}", command.recipientUserId());

            var newNotification = new Notification(
                    command.recipientUserId(),
                    command.subject(),
                    command.messageBody()
            );

            var savedNotification = notificationRepository.save(newNotification);
            log.info("[NotificationCommandService] Notification created successfully with ID: {}", savedNotification.getId());

            if (command.sendEmail()) {
                sendEmailForNotification(command);
            }

            return Optional.of(savedNotification);
        } catch (Exception e) {
            log.error("[NotificationCommandService] Error creating notification: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error creating notification: " + e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void sendEmailForNotification(CreateNotificationCommand command) {
        try {
            log.debug("[NotificationCommandService] Sending email for notification");

            if (command.attachmentPath() != null && !command.attachmentPath().isBlank()) {
                emailService.sendEmailWithAttachment(
                        command.recipientEmail(),
                        command.subject(),
                        command.messageBody(),
                        command.attachmentPath()
                );
                log.info("[NotificationCommandService] Email with attachment sent successfully");
            } else {
                emailService.sendEmail(
                        command.recipientEmail(),
                        command.subject(),
                        command.messageBody()
                );
                log.info("[NotificationCommandService] Email sent successfully");
            }
        } catch (Exception e) {
            log.error("[NotificationCommandService] Error sending email: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage(), e);
        }
    }
}
