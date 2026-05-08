package com.parkingit.cloud.reservations.interfaces.rest;

import com.parkingit.cloud.reservations.domain.model.commands.CancelReservationCommand;
import com.parkingit.cloud.reservations.domain.model.commands.ClaimReservationCommand;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsByParkingIdQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetAllReservationsByUserIdQuery;
import com.parkingit.cloud.reservations.domain.model.queries.GetReservationByIdQuery;
import com.parkingit.cloud.reservations.domain.services.ReservationCommandService;
import com.parkingit.cloud.reservations.domain.services.ReservationQueryService;
import com.parkingit.cloud.reservations.interfaces.rest.resources.*;
import com.parkingit.cloud.reservations.interfaces.rest.transform.CreateReservationCommandFromResourceAssembler;
import com.parkingit.cloud.reservations.interfaces.rest.transform.ReservationResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@AllArgsConstructor
@Slf4j
@Tag(name = "Reservations", description = "Parking reservations management endpoints")
public class ReservationsController {
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    @PostMapping("")
    @Operation(
        summary = "Create a new parking reservation",
        description = "Creates a reservation for today and immediately sends the access code to the user's email. " +
                      "The code must be presented at the parking entrance within 15 minutes of the reserved arrival time."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created — access code sent by email",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request — date must be today, time must be in the future"),
            @ApiResponse(responseCode = "409", description = "Conflict — you already have an active or pending reservation for this parking lot")
    })
    public ResponseEntity<ReservationResource> createReservation(@RequestBody CreateReservationResource resource) {
        try {
            var command = CreateReservationCommandFromResourceAssembler.toCommandFromResource(resource);
            var reservation = reservationCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Failed to create reservation"));

            var response = ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            log.warn("[ReservationsController] Conflict creating reservation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("[ReservationsController] Invalid reservation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("[ReservationsController] Error creating reservation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResource.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ResponseEntity<ReservationResource> getReservationById(
            @Parameter(description = "Reservation unique identifier") @PathVariable UUID id) {
        try {
            var reservation = reservationQueryService.handle(new GetReservationByIdQuery(id));
            if (reservation.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation.get()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reservations by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user reservations")
    })
    public ResponseEntity<List<ReservationResource>> getReservationsByUserId(
            @Parameter(description = "User unique identifier") @PathVariable UUID userId) {
        try {
            var reservations = reservationQueryService.handle(new GetAllReservationsByUserIdQuery(userId));
            var responses = reservations.stream()
                    .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parking/{parkingId}")
    @Operation(summary = "Get reservations by parking ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of parking reservations")
    })
    public ResponseEntity<List<ReservationResource>> getReservationsByParkingId(
            @Parameter(description = "Parking unique identifier") @PathVariable UUID parkingId) {
        try {
            var reservations = reservationQueryService.handle(new GetAllReservationsByParkingIdQuery(parkingId));
            var responses = reservations.stream()
                    .map(ReservationResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/claim")
    @Operation(
        summary = "Claim reservation at parking entrance",
        description = "The parking operator enters the access code the customer presents. " +
                      "If valid and within the 15-minute grace period, the reservation becomes ACTIVE and billing starts. " +
                      "If the grace period has passed, the reservation is automatically marked as EXPIRED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation claimed — billing started",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResource.class))),
            @ApiResponse(responseCode = "400", description = "Code not found, already used, or reservation not in PENDING state"),
            @ApiResponse(responseCode = "409", description = "Reservation expired — user arrived after the 15-minute grace period")
    })
    public ResponseEntity<ReservationResource> claimReservation(@RequestBody ClaimReservationResource resource) {
        try {
            var command = new ClaimReservationCommand(resource.code());
            var reservation = reservationCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Failed to claim reservation"));

            return ResponseEntity.ok(ReservationResourceFromEntityAssembler.toResourceFromEntity(reservation));
        } catch (IllegalStateException e) {
            log.warn("[ReservationsController] Claim failed (grace period or state): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("[ReservationsController] Invalid claim attempt: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("[ReservationsController] Error claiming reservation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel this reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Reservation unique identifier") @PathVariable UUID id,
            @RequestParam(defaultValue = "Cancelled by user") String reason
    ) {
        try {
            reservationCommandService.handle(new CancelReservationCommand(id, reason));
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.warn("[ReservationsController] Cannot cancel: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("[ReservationsController] Error cancelling reservation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
