package com.parkingit.edge.recognition.interfaces.rest;

import com.parkingit.edge.recognition.domain.model.commands.ActivateRecognition;
import com.parkingit.edge.recognition.domain.model.commands.ActivateRecognitionCommand;
import com.parkingit.edge.recognition.domain.model.commands.DeactivateRecognitionCommand;
import com.parkingit.edge.recognition.domain.model.queries.GetLatestStatusQuery;
import com.parkingit.edge.recognition.domain.services.RecognitionManagementService;
import com.parkingit.edge.recognition.interfaces.rest.resources.ActivateRecognitionResource;
import com.parkingit.edge.recognition.interfaces.rest.resources.DeactivateRecognitionResource;
import com.parkingit.edge.recognition.interfaces.rest.resources.RecognitionSessionResource;
import com.parkingit.edge.recognition.interfaces.rest.resources.RecognitionStatusResource;
import com.parkingit.edge.recognition.interfaces.rest.transform.RecognitionSessionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.GET, RequestMethod.DELETE})
@RestController
@RequestMapping(value = "/api/v1/recognition", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Recognition Management", description = "License Plate and Facial Recognition - Activate/Deactivate sensors")
public class RecognitionsController {
    private final RecognitionManagementService managementService;

    @PostMapping("/activate")
    @Operation(summary = "Activate LPR sensor", description = "Activates license plate and/or facial recognition for a specific parking lot. Returns session with timeout information")
    @ApiResponse(responseCode = "201", description = "LPR activated successfully", content = @Content(schema = @Schema(implementation = RecognitionSessionResource.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    public ResponseEntity<RecognitionSessionResource> activateLpr(@Valid @RequestBody ActivateRecognitionResource resource) {
        var command = new ActivateRecognitionCommand(
                resource.parkingId(),
                resource.driverId(),
                resource.timeoutSeconds() != null ? resource.timeoutSeconds() : 30
        );

        var session = managementService.handle(command);
        var recognitionSessionResource = RecognitionSessionResourceFromEntityAssembler.toResourceFromEntity(session);

        return new ResponseEntity<>(recognitionSessionResource, HttpStatus.CREATED);
    }

    @PostMapping("/deactivate")
    @Operation(summary = "Deactivate LPR sensor", description = "Manually deactivates an active LPR session")
    @ApiResponse(responseCode = "200", description = "LPR deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<?> deactivateLpr(@Valid @RequestBody DeactivateRecognitionResource resource) {
        var command = new DeactivateRecognitionCommand(
                resource.sessionId()
        );

        managementService.handle(command);

        return ResponseEntity.ok(Map.of("status", "success", "message", "LPR session deactivated"));
    }

    @GetMapping("/status/{parkingId}")
    @Operation(summary = "Get LPR status", description = "Retrieves current LPR session status for a specific parking lot")
    @ApiResponse(responseCode = "200", description = "Status retrieved", content = @Content(schema = @Schema(implementation = RecognitionStatusResource.class)))
    @ApiResponse(responseCode = "404", description = "No active session found")
    public ResponseEntity<?> getRecognitionStatus(@PathVariable UUID parkingId) {
        var activeSession = managementService.getActiveSession(parkingId);

        if (activeSession.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "parkingId", parkingId,
                    "status", "INACTIVE",
                    "isActive", false,
                    "message", "No active LPR session"
            ));
        }

        var session = activeSession.get();
        var resource = new RecognitionStatusResource(
                session.getId(),
                session.getParkingId(),
                session.getStatus().name(),
                session.getTimedOut(),
                session.getDeactivatedAt(),
                session.getTimeoutAt()
        );

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/health")
    @Operation(summary = "Recognition service health", description = "Check if recognition service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "lpr-service",
                "timestamp", java.time.LocalDateTime.now()
        ));
    }

    @PostMapping("/quick-activate")
    @Operation(summary = "Activate LPR sensor", description = "Activates recognition - returns true. Auto-deactivates after 10 seconds")
    @ApiResponse(responseCode = "201", description = "LPR activated - returns true")
    public ResponseEntity<Boolean> activateLpr() {
        var command = new ActivateRecognition();
        Boolean isActive = managementService.handle(command);

        return new ResponseEntity<>(isActive, HttpStatus.CREATED);
    }

    @GetMapping("/status")
    @Operation(
            summary = "Get Recognition Status (for Python polling)",
            description = "Returns current recognition status. Python calls this intermittently to detect activation"
    )
    @ApiResponse(responseCode = "200", description = "Status retrieved")
    public ResponseEntity<Map<String, Object>> getRecognitionStatus() {
        Boolean isActive = managementService.handle(new GetLatestStatusQuery());

        return ResponseEntity.ok(Map.of(
                "isActive", isActive,
                "timestamp", System.currentTimeMillis()
        ));
    }
}
