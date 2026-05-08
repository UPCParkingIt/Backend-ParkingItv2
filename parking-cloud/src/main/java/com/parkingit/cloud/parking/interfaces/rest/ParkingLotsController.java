package com.parkingit.cloud.parking.interfaces.rest;

import com.parkingit.cloud.parking.application.internal.outboundservices.acl.ExternalLogService;
import com.parkingit.cloud.parking.domain.exceptions.ParkingNotFoundException;
import com.parkingit.cloud.parking.domain.model.commands.CreateParkingCommand;
import com.parkingit.cloud.parking.domain.model.commands.DeactivateParkingCommand;
import com.parkingit.cloud.parking.domain.model.commands.UpdateParkingCommand;
import com.parkingit.cloud.parking.domain.model.queries.*;
import com.parkingit.cloud.parking.domain.services.ParkingCommandService;
import com.parkingit.cloud.parking.domain.services.ParkingQueryService;
import com.parkingit.cloud.parking.interfaces.rest.resources.*;
import com.parkingit.cloud.parking.interfaces.rest.transform.ParkingResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE })
@RequestMapping(value = "/api/v1/parking-lots", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Parking Lots", description = "Parking Lot Management - Create, update, and retrieve parking lot information and statistics")
public class ParkingLotsController {
    private final ParkingCommandService parkingCommandService;
    private final ParkingQueryService parkingQueryService;
    private final ExternalLogService externalLogService;

    /**
     * Creates a new parking lot with initial configuration.
     * Only parking administrators can create parking lots.
     *
     * @param resource the request containing parking details (name, location, spots, tariff, hours)
     * @return the created ParkingResource with status 201
     */
    @PostMapping
    @Operation(summary = "Create a new parking lot", description = "Creates a new parking lot with all configuration details (location, number of spots, tariff, operating hours)")
    @ApiResponse(responseCode = "201", description = "Parking lot created successfully", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    @ApiResponse(responseCode = "400", description = "Invalid parking data")
    public ResponseEntity<ParkingResource> createParking(@RequestBody CreateParkingResource resource) {
        var parking = parkingCommandService.handle(new CreateParkingCommand(
                resource.parkingName(),
                resource.latitude(),
                resource.longitude(),
                resource.address(),
                resource.totalSpots(),
                resource.baseTariffPerHour(),
                resource.currency(),
                resource.openTime(),
                resource.closeTime(),
                resource.businessDays(),
                resource.adminUserId()
        ));

        if (parking.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var parkingResource = ParkingResourceFromEntityAssembler.toResourceFromEntity(parking.get());

        return new ResponseEntity<>(parkingResource, HttpStatus.CREATED);
    }

    /**
     * Updates an existing parking lot's configuration.
     * Allows updating name, location, operating hours, and tariff.
     *
     * @param id the parking lot's unique identifier
     * @param resource the request containing updated parking details
     * @return the updated ParkingResource
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Update a parking lot", description = "Updates an existing parking lot's configuration (name, location, hours, tariff)")
    @ApiResponse(responseCode = "200", description = "Parking lot updated successfully", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    @ApiResponse(responseCode = "400", description = "Invalid update data")
    @ApiResponse(responseCode = "404", description = "Parking lot not found")
    public ResponseEntity<ParkingResource> updateParking(@PathVariable UUID id, @RequestBody UpdateParkingResource resource) {
        var updatedParking = parkingCommandService.handle(new UpdateParkingCommand(
                id,
                resource.parkingName(),
                resource.latitude(),
                resource.longitude(),
                resource.address(),
                resource.openTime(),
                resource.closeTime(),
                resource.businessDays(),
                resource.newTariff()
        ));

        if (updatedParking.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var parkingResource = ParkingResourceFromEntityAssembler.toResourceFromEntity(updatedParking.get());

        return ResponseEntity.ok(parkingResource);
    }

    /**
     * Deactivates a parking lot (soft delete).
     * The parking lot remains in the system but is no longer available for new reservations.
     *
     * @param parkingId the parking lot's unique identifier
     * @return success message
     */
    @DeleteMapping("/{parkingId}")
    @Operation(summary = "Deactivate a parking lot", description = "Deactivates a parking lot (soft delete). It remains in records but is unavailable for new reservations")
    @ApiResponse(responseCode = "200", description = "Parking lot deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Parking lot not found")
    public ResponseEntity<?> deactivateParking(@PathVariable UUID parkingId) {
        parkingCommandService.handle(new DeactivateParkingCommand(parkingId));

        return ResponseEntity.ok("Parking lot deactivated successfully");
    }

    /**
     * Retrieves a specific parking lot by its unique identifier.
     *
     * @param id the parking lot's unique identifier
     * @return the ParkingResource with all details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get parking lot by ID", description = "Retrieves complete details of a specific parking lot")
    @ApiResponse(responseCode = "200", description = "Parking lot found", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    @ApiResponse(responseCode = "404", description = "Parking lot not found")
    public ResponseEntity<ParkingResource> getParkingById(@PathVariable UUID id) {
        var parking = parkingQueryService.handle(new GetParkingByIdQuery(id));

        if (parking.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var parkingResource = ParkingResourceFromEntityAssembler.toResourceFromEntity(parking.get());

        return ResponseEntity.ok(parkingResource);
    }

    /**
     * Retrieves the parking lot assigned to a specific administrator.
     * Each admin user is assigned exactly one parking lot.
     *
     * @param adminUserId the administrator user's unique identifier
     * @return the ParkingResource for that administrator
     */
    @GetMapping("/admin/{adminUserId}")
    @Operation(summary = "Get parking lot by admin ID", description = "Retrieves the parking lot assigned to a specific administrator")
    @ApiResponse(responseCode = "200", description = "Parking lot found", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    @ApiResponse(responseCode = "404", description = "Parking lot not found for this administrator")
    public ResponseEntity<ParkingResource> getParkingByAdminUserId(@PathVariable UUID adminUserId) {
        var parking = parkingQueryService.handle(new GetParkingByAdminUserIdQuery(adminUserId));

        if (parking.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var parkingResource = ParkingResourceFromEntityAssembler.toResourceFromEntity(parking.get());

        return ResponseEntity.ok(parkingResource);
    }

    /**
     * Retrieves a list of all parking lots in the system.
     * Can be filtered by active/inactive status.
     *
     * @return list of all ParkingResources
     */
    @GetMapping
    @Operation(summary = "Get all parking lots", description = "Retrieves a list of all parking lots in the system (both active and inactive)")
    @ApiResponse(responseCode = "200", description = "Parking lots retrieved successfully", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    public ResponseEntity<List<ParkingResource>> getAllParkingLots() {
        var parkingLots = parkingQueryService.handle(new GetAllParkingLotsQuery());

        var parkingLotResources = parkingLots.stream()
                .map(ParkingResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(parkingLotResources);
    }

    /**
     * Retrieves all inactive/deactivated parking lots.
     * Useful for admin dashboard to see archived parking lots.
     *
     * @return list of inactive ParkingResources
     */
    @GetMapping("/inactive")
    @Operation(summary = "Get inactive parking lots", description = "Retrieves all deactivated/archived parking lots")
    @ApiResponse(responseCode = "200", description = "Inactive parking lots retrieved successfully", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    public ResponseEntity<List<ParkingResource>> getAllInactiveParkingLots() {
        var parkingLots = parkingQueryService.handle(new GetAllInactiveParkingLotsQuery());

        var parkingLotsResources = parkingLots.stream()
                .map(ParkingResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(parkingLotsResources);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active parking lots", description = "Retrieves all currently active parking lots available for reservations")
    @ApiResponse(responseCode = "200", description = "Active parking lots retrieved successfully", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    public ResponseEntity<List<ParkingResource>> getAllActiveParkingLots() {
        var parkingLots = parkingQueryService.handle(new GetAllActiveParkingLotsQuery());

        var parkingLotsResources = parkingLots.stream()
                .map(ParkingResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(parkingLotsResources);
    }

    @GetMapping("/search")
    @Operation(summary = "Search parking lots by name", description = "Searches for parking lots that match the provided name (partial or full match)")
    @ApiResponse(responseCode = "200", description = "Parking lots retrieved successfully", content = @Content(schema = @Schema(implementation = ParkingResource.class)))
    public ResponseEntity<List<ParkingResource>> getAllParkingLotsByName(@RequestParam String name) {
        var parkingLots = parkingQueryService.handle(new GetAllParkingLotsByNameQuery(name));

        var parkingLotsResources = parkingLots.stream()
                .map(ParkingResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(parkingLotsResources);
    }

    @GetMapping("/{id}/occupancy")
    @Operation(summary = "Get occupancy info for parking ID", description = "Retrieves current occupancy information for a specific parking lot, including available spots, total spots, occupancy percentage, and status")
    @ApiResponse(responseCode = "200", description = "Occupancy info retrieved successfully", content = @Content(schema = @Schema(implementation = OccupancyInfoResource.class)))
    @ApiResponse(responseCode = "404", description = "Parking lot not found with the provided ID")
    public ResponseEntity<OccupancyInfoResource> getOccupancyInfoByParkingId(@PathVariable UUID id) {
        var parking = parkingQueryService.handle(new GetParkingByIdQuery(id));

        if (parking.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var occupancy = new OccupancyInfoResource(
                parking.get().getAvailableSpots(),
                parking.get().getTotalSpots(),
                parking.get().getOccupancyPercentage(),
                parking.get().getStatus()
        );

        return ResponseEntity.ok(occupancy);
    }

    @GetMapping("/{parkingId}/stats")
    @Operation(summary = "Get stats for its parking ID", description = "Recover stats for occupancy and alerts")
    @ApiResponse(responseCode = "200", description = "Stats retrieved successfully", content = @Content(schema = @Schema(implementation = OccupancyStatsResource.class)))
    public ResponseEntity<?> getParkingStats(@PathVariable UUID parkingId) {
        try {
            var stats = externalLogService.fetchOccupancyStatsByParkingId(parkingId);

            var response = new OccupancyStatsResource(
                    stats.getTotalEntries(),
                    stats.getTotalExits(),
                    stats.getMatchedExits(),
                    stats.getFailedExits(),
                    stats.getAlerts(),
                    stats.getAverageOccupancyMinutes(),
                    stats.getOccupancyRate()
            );

            return ResponseEntity.ok(response);
        } catch (ParkingNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
