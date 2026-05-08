package com.parkingit.cloud.notifications.interfaces.acl;

import com.parkingit.cloud.iam.domain.model.aggregates.User;

public interface NotificationContextFacade {
    void createNotification(User recipientUser, String subject, String messageBody, String attachmentPath, Boolean sendEmail);
}
