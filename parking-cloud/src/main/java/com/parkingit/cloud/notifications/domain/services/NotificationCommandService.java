package com.parkingit.cloud.notifications.domain.services;

import com.parkingit.cloud.notifications.domain.model.aggregates.Notification;
import com.parkingit.cloud.notifications.domain.model.commands.CreateNotificationCommand;

import java.util.Optional;

public interface NotificationCommandService {
    Optional<Notification> handle(CreateNotificationCommand command);
}
