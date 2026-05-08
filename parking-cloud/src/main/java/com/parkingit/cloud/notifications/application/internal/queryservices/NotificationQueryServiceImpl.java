package com.parkingit.cloud.notifications.application.internal.queryservices;

import com.parkingit.cloud.notifications.domain.model.aggregates.Notification;
import com.parkingit.cloud.notifications.domain.model.queries.GetAllNotificationsByRecipientUserIdQuery;
import com.parkingit.cloud.notifications.domain.model.queries.GetAllNotificationsQuery;
import com.parkingit.cloud.notifications.domain.services.NotificationQueryService;
import com.parkingit.cloud.notifications.infrastructure.persistence.jpa.repositories.NotificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationQueryServiceImpl implements NotificationQueryService {
    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> handle(GetAllNotificationsQuery query) {
        return notificationRepository.findAll();
    }

    @Override
    public List<Notification> handle(GetAllNotificationsByRecipientUserIdQuery query) {
        return notificationRepository.findAllByRecipientUserId(query.recipientUserId());
    }
}
