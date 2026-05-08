package com.parkingit.cloud.payments.application.internal.outboundservices.acl;

import com.parkingit.cloud.notifications.interfaces.acl.NotificationContextFacade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service("paymentsNotificationService")
@AllArgsConstructor
public class ExternalNotificationService {
    private final NotificationContextFacade notificationContextFacade;
}
