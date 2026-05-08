package com.parkingit.cloud.notifications.domain.services;

import com.parkingit.cloud.notifications.domain.model.aggregates.Notification;
import com.parkingit.cloud.notifications.domain.model.queries.GetAllNotificationsByRecipientUserIdQuery;
import com.parkingit.cloud.notifications.domain.model.queries.GetAllNotificationsQuery;

import java.util.List;

public interface NotificationQueryService {
    List<Notification> handle(GetAllNotificationsQuery query);
    List<Notification> handle(GetAllNotificationsByRecipientUserIdQuery query);
}
