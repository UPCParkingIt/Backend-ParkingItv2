package com.parkingit.cloud.notifications.interfaces.rest;

import com.parkingit.cloud.notifications.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.notifications.domain.model.queries.GetAllNotificationsByRecipientUserIdQuery;
import com.parkingit.cloud.notifications.domain.model.queries.GetAllNotificationsQuery;
import com.parkingit.cloud.notifications.domain.services.NotificationCommandService;
import com.parkingit.cloud.notifications.domain.services.NotificationQueryService;
import com.parkingit.cloud.notifications.interfaces.rest.resources.NotificationResource;
import com.parkingit.cloud.notifications.interfaces.rest.transform.NotificationResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "User Notifications - Retrieve and manage system notifications")
public class NotificationsController {
    private final NotificationCommandService commandService;
    private final NotificationQueryService queryService;
    private final ExternalIamService externalIamService;

    /**
     * Retrieves all notifications in the system (admin only).
     *
     * @return list of all NotificationResources
     */
    @GetMapping()
    @Operation(summary = "Get all notifications", description = "Retrieves all system notifications. Typically admin-only endpoint")
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully", content = @Content(schema = @Schema(implementation = NotificationResource.class)))
    public ResponseEntity<List<NotificationResource>> getAllNotifications() {
        var getAllNotificationsQuery = new GetAllNotificationsQuery();
        var notifications = queryService.handle(getAllNotificationsQuery);
        var notificationResources = notifications.stream()
                .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(notificationResources);
    }

    /**
     * Retrieves all notifications for a specific user.
     * Includes alerts, reservations, promotions, and system notifications.
     *
     * @param recipientUserId the user's unique identifier
     * @return list of NotificationResources for the user
     */
    @GetMapping("/{recipientUserId}")
    @Operation(summary = "Get user notifications", description = "Retrieves all notifications for a specific user (alerts, reservations, promotions, etc.)")
    @ApiResponse(responseCode = "200", description = "User notifications retrieved successfully", content = @Content(schema = @Schema(implementation = NotificationResource.class)))
    public ResponseEntity<List<NotificationResource>> getAllNotificationsByRecipientUserId(@PathVariable UUID recipientUserId) {
        var  getAllNotificationsQuery = new GetAllNotificationsByRecipientUserIdQuery(recipientUserId);
        var notifications = queryService.handle(getAllNotificationsQuery);
        var notificationResources = notifications.stream()
                .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(notificationResources);
    }
}
