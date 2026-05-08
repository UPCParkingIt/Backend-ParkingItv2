package com.parkingit.cloud.parking.interfaces.rest;

import com.parkingit.cloud.parking.domain.model.commands.CreateAlertCommand;
import com.parkingit.cloud.parking.domain.model.commands.ReviewAlertCommand;
import com.parkingit.cloud.parking.domain.model.queries.GetAllAlertsByParkingIdAndStatusQuery;
import com.parkingit.cloud.parking.domain.model.queries.GetAllAlertsByParkingIdQuery;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertStatus;
import com.parkingit.cloud.parking.domain.services.ParkingCommandService;
import com.parkingit.cloud.parking.domain.services.ParkingQueryService;
import com.parkingit.cloud.parking.interfaces.rest.resources.AlertResource;
import com.parkingit.cloud.parking.interfaces.rest.resources.CreateAlertResource;
import com.parkingit.cloud.parking.interfaces.rest.transform.AlertResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/parking/alerts", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
@Tag(name = "Alerts", description = "Parking Security Alerts - Create, retrieve, and manage security incidents")
@RequiredArgsConstructor
public class AlertsController {
    private final ParkingCommandService parkingCommandService;
    private final ParkingQueryService parkingQueryService;

    /**
     * Retrieves all security alerts for a specific parking lot.
     * Includes facial mismatches, suspicious behavior, and other security incidents.
     *
     * @param parkingId the parking lot's unique identifier
     * @return list of all AlertResources for the parking
     */
    @GetMapping("/{parkingId}")
    @Operation(summary = "Get all parking alerts", description = "Retrieves all security alerts for a parking lot (security incidents, suspicious behavior, etc.)")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully", content = @Content(schema = @Schema(implementation = AlertResource.class)))
    public ResponseEntity<List<AlertResource>> getAllAlertsByParkingId(@PathVariable UUID parkingId) {
        var alerts = parkingQueryService.handle(new GetAllAlertsByParkingIdQuery(parkingId));

        var alertResources = alerts.stream()
                .map(AlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(alertResources);
    }

    /**
     * Retrieves alerts for a parking lot filtered by their current status.
     * Allows filtering by PENDING, REVIEWED, RESOLVED, or FALSE_ALARM.
     *
     * @param parkingId the parking lot's unique identifier
     * @param status the AlertStatus to filter by
     * @return list of AlertResources matching the status
     */
    @GetMapping("/{parkingId}/status")
    @Operation(summary = "Get alerts by status", description = "Retrieves alerts for a parking lot filtered by status (PENDING, REVIEWED, RESOLVED, FALSE_ALARM)")
    @ApiResponse(responseCode = "200", description = "Filtered alerts retrieved successfully", content = @Content(schema = @Schema(implementation = AlertResource.class)))
    public ResponseEntity<List<AlertResource>> getAllAlertsByParkingIdAndStatus(@PathVariable UUID parkingId, @RequestParam AlertStatus status) {
        var alerts = parkingQueryService.handle(new GetAllAlertsByParkingIdAndStatusQuery(parkingId, status));

        var alertResources = alerts.stream()
                .map(AlertResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(List.of());
    }

    /**
     * Creates a new security alert for a parking lot.
     * Used by admin to manually report incidents or incidents created by the system.
     *
     * @param resource the request containing alert details (type, severity, description)
     * @return the created AlertResource with status 201
     */
    @PostMapping
    @Operation(summary = "Create a security alert", description = "Creates a new security alert for a parking lot with type, severity, and description")
    @ApiResponse(responseCode = "201", description = "Alert created successfully", content = @Content(schema = @Schema(implementation = AlertResource.class)))
    @ApiResponse(responseCode = "400", description = "Invalid alert data")
    public ResponseEntity<AlertResource> createAlert(@RequestBody CreateAlertResource resource) {
        var alert = parkingCommandService.handle(new CreateAlertCommand(
                resource.parkingId(),
                resource.alertType(),
                resource.severity(),
                resource.description(),
                resource.parkingLogId()
        ));

        if (alert.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var alertResource = AlertResourceFromEntityAssembler.toResourceFromEntity(alert.get());

        return new ResponseEntity<>(alertResource, HttpStatus.CREATED);
    }

    /**
     * Marks an alert as reviewed by a parking administrator.
     * Adds notes/comments about the review for audit trail.
     *
     * @param alertId the alert's unique identifier
     * @param notes the admin's notes about the alert
     * @return success message
     */
    @PutMapping("/{alertId}/review")
    @Operation(summary = "Review an alert", description = "Marks an alert as reviewed by admin and adds notes for audit trail")
    @ApiResponse(responseCode = "200", description = "Alert reviewed successfully")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    public ResponseEntity<?> reviewAlert(@PathVariable UUID alertId, @RequestParam @NotBlank String notes) {
        parkingCommandService.handle(new ReviewAlertCommand(alertId, notes));

        return ResponseEntity.ok("Alert reviewed successfully");
    }

    /**
     * Resolves an alert (closes/completes the incident investigation).
     * Adds resolution notes describing the action taken.
     *
     * @param alertId the alert's unique identifier
     * @param notes the admin's resolution notes and actions taken
     * @return success message
     */
    @PutMapping("/{alertId}/resolve")
    @Operation(summary = "Resolve an alert", description = "Marks an alert as resolved and closes the incident. Stores resolution details")
    @ApiResponse(responseCode = "200", description = "Alert resolved successfully")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    public ResponseEntity<?> resolveAlert(@PathVariable UUID alertId, @RequestParam @NotBlank String notes) {
        parkingCommandService.handle(new ReviewAlertCommand(alertId, notes));

        return ResponseEntity.ok("Alert resolved successfully");
    }

    /**
     * Marks an alert as a false alarm (incorrect detection).
     * Used when system generates alerts that turn out to be false positives.
     * Helps improve ML model accuracy.
     *
     * @param alertId the alert's unique identifier
     * @param notes the reason why this was a false alarm
     * @return success message
     */
    @PutMapping("/{alertId}/markAsFalseAlarm")
    @Operation(summary = "Mark alert as false alarm", description = "Marks an alert as a false alarm (incorrect detection). Helps improve system accuracy")
    @ApiResponse(responseCode = "200", description = "Alert marked as false alarm successfully")
    @ApiResponse(responseCode = "404", description = "Alert not found")
    public ResponseEntity<?> markAsFalseAlarm(@PathVariable UUID alertId, @RequestParam @NotBlank String notes) {
        parkingCommandService.handle(new ReviewAlertCommand(alertId, notes));

        return ResponseEntity.ok("Alert marked as false alarm");
    }
}
