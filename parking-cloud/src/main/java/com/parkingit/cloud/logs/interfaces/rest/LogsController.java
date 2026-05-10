package com.parkingit.cloud.logs.interfaces.rest;

import com.parkingit.cloud.logs.domain.model.commands.RecordEntryLogCommand;
import com.parkingit.cloud.logs.domain.model.queries.GetAllAlertsGeneratedByParkingIdQuery;
import com.parkingit.cloud.logs.domain.model.queries.GetAllParkingLogsByParkingIdQuery;
import com.parkingit.cloud.logs.domain.model.queries.GetParkingLogByIdQuery;
import com.parkingit.cloud.logs.domain.services.LogCommandService;
import com.parkingit.cloud.logs.domain.services.LogQueryService;
import com.parkingit.cloud.logs.interfaces.rest.resources.AlertLogResource;
import com.parkingit.cloud.logs.interfaces.rest.resources.CreateRecordEntryLogResource;
import com.parkingit.cloud.logs.interfaces.rest.resources.CreateRecordExitLogResource;
import com.parkingit.cloud.logs.interfaces.rest.resources.LogResource;
import com.parkingit.cloud.logs.interfaces.rest.transform.AlertLogResourceFromEntityAssembler;
import com.parkingit.cloud.logs.interfaces.rest.transform.LogResourceFromEntityAssembler;
import com.parkingit.cloud.logs.interfaces.rest.transform.RecordEntryLogCommandFromResourceAssembler;
import com.parkingit.cloud.logs.interfaces.rest.transform.RecordExitLogCommandFromResourceAssembler;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/api/v1/logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Logs", description = "Parking Entry and Exit Logs - Record vehicle entry/exit with facial verification")
public class LogsController {
    private final LogCommandService logCommandService;
    private final LogQueryService logQueryService;

    /**
     * Records a vehicle entry into the parking lot with license plate and facial recognition data.
     * Automatically updates parking occupancy and triggers entry event.
     *
     * @param resource the entry request containing plate, facial embedding, and parking ID
     * @return the created ParkingLog with entry details
     */
    @PostMapping("/entry")
    @Operation(summary = "Record vehicle entry", description = "Registers a vehicle entry with license plate recognition and facial embedding. Occupies a parking spot and creates an entry log")
    @ApiResponse(responseCode = "201", description = "Entry recorded successfully", content = @Content(schema = @Schema(implementation = LogResource.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed - invalid plate or embedding")
    public ResponseEntity<LogResource> recordEntry(@Valid @RequestBody CreateRecordEntryLogResource resource) {
        var recordEntryLogCommand = RecordEntryLogCommandFromResourceAssembler.toCommandFromResource(resource);
        var entryLog = logCommandService.handle(recordEntryLogCommand);

        if (entryLog.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var entryLogResource = LogResourceFromEntityAssembler.toResourceFromEntity(entryLog.get());
        return new ResponseEntity<>(entryLogResource, HttpStatus.CREATED);
    }

    /**
     * Records a vehicle exit from the parking lot with facial verification against entry data.
     * Generates security alerts if facial data doesn't match entry, releases parking spot.
     *
     * @param resource the exit request containing entry log ID, plate, facial embedding, and verification result
     * @return the updated ParkingLog with exit details and alert status
     */
    @PostMapping("/exit")
    @Operation(summary = "Record vehicle exit", description = "Registers vehicle exit with facial verification against entry data. Detects mismatches (potential security issue). Releases parking spot and calculates occupancy duration")
    @ApiResponse(responseCode = "201", description = "Exit recorded successfully", content = @Content(schema = @Schema(implementation = LogResource.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Entry log not found")
    public ResponseEntity<LogResource> recordExit(@Valid @RequestBody CreateRecordExitLogResource resource) {
        var recordExitLogCommand = RecordExitLogCommandFromResourceAssembler.toCommandFromResource(resource);
        var exitLog = logCommandService.handle(recordExitLogCommand);

        if (exitLog.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var exitLogResource = LogResourceFromEntityAssembler.toResourceFromEntity(exitLog.get());
        return new ResponseEntity<>(exitLogResource, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific parking log by its unique identifier.
     *
     * @param id the unique identifier of the parking log
     * @return the ParkingLog with entry, exit, and verification details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get parking log by ID", description = "Retrieves a complete parking log including entry time, exit time, facial verification status, and any alerts")
    @ApiResponse(responseCode = "200", description = "Log found", content = @Content(schema = @Schema(implementation = LogResource.class)))
    @ApiResponse(responseCode = "404", description = "Log not found")
    public ResponseEntity<LogResource> getLogById(@PathVariable UUID id) {
        var log = logQueryService.handle(new GetParkingLogByIdQuery(id));
        if (log.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var resource = LogResourceFromEntityAssembler.toResourceFromEntity(log.get());
        return ResponseEntity.ok(resource);
    }

    /**
     * Retrieves all entry and exit logs for a specific parking lot.
     * Used for audit trail, reporting, and occupancy tracking.
     *
     * @param parkingId the parking lot's unique identifier
     * @return list of all ParkingLogs for that parking lot
     */
    @GetMapping("/parking/{parkingId}")
    @Operation(summary = "Get all logs for a parking lot", description = "Retrieves all entry and exit logs for a specific parking lot, ordered by most recent first")
    @ApiResponse(responseCode = "200", description = "Logs retrieved successfully", content = @Content(schema = @Schema(implementation = LogResource.class)))
    public ResponseEntity<List<LogResource>> getLogsByParkingId(@PathVariable UUID parkingId) {
        var log = logQueryService.handle(new GetAllParkingLogsByParkingIdQuery(parkingId));
        if (log.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<LogResource> resources = log.stream()
                .map(LogResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves all security alerts generated from log verification failures (facial mismatches).
     * These are alerts generated when exit facial data doesn't match entry data.
     *
     * @param parkingId the parking lot's unique identifier
     * @return list of AlertLogResources for facial mismatches
     */
    @GetMapping("/parking/{parkingId}/alerts")
    @Operation(summary = "Get facial mismatch alerts from logs", description = "Retrieves all alerts generated by log verification (facial mismatches detected on vehicle exit)")
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully", content = @Content(schema = @Schema(implementation = AlertLogResource.class)))
    public ResponseEntity<List<AlertLogResource>> getAlertsByParkingId(@PathVariable UUID parkingId) {
        var log = logQueryService.handle(new GetAllAlertsGeneratedByParkingIdQuery(parkingId));
        if (log.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<AlertLogResource> resources = log.stream()
                .map(AlertLogResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }
}
