package com.parkingit.cloud.notifications.domain.model.queries;

import java.util.UUID;

public record GetAllNotificationsByRecipientUserIdQuery(UUID recipientUserId) {
}
