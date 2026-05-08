package com.parkingit.cloud.parking.interfaces.rest;

import com.parkingit.cloud.parking.domain.model.commands.CreatePromotionCommand;
import com.parkingit.cloud.parking.domain.model.commands.DeactivatePromotionCommand;
import com.parkingit.cloud.parking.domain.model.queries.GetAllPromotionsByParkingIdQuery;
import com.parkingit.cloud.parking.domain.services.ParkingCommandService;
import com.parkingit.cloud.parking.domain.services.ParkingQueryService;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.PromotionRepository;
import com.parkingit.cloud.parking.interfaces.rest.resources.CreatePromotionResource;
import com.parkingit.cloud.parking.interfaces.rest.resources.PromotionResource;
import com.parkingit.cloud.parking.interfaces.rest.transform.PromotionResourceFromEntityAssembler;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/promotions", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Tag(name = "Promotions", description = "Parking Promotions Management")
@RequiredArgsConstructor
public class PromotionsController {
    private final ParkingCommandService parkingCommandService;
    private final ParkingQueryService parkingQueryService;
    private final PromotionRepository promotionRepository;

    @GetMapping("/{parkingId}")
    @Operation(summary = "Get all promotions for a parking")
    @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionResource.class)))
    public ResponseEntity<List<PromotionResource>> getAllPromotionsByParkingId(@PathVariable UUID parkingId) {
        var promotions = parkingQueryService.handle(new GetAllPromotionsByParkingIdQuery(parkingId));

        var promotionResources = promotions.stream()
                .map(PromotionResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(promotionResources);
    }

    @PostMapping
    @Operation(summary = "Create a new promotion")
    @ApiResponse(responseCode = "201", description = "Promotion created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PromotionResource.class)))
    public ResponseEntity<PromotionResource> createPromotion(@RequestBody CreatePromotionResource resource) {
        var promotion = parkingCommandService.handle(new CreatePromotionCommand(
                resource.parkingId(),
                resource.title(),
                resource.description(),
                resource.discountPercent(),
                resource.validFrom(),
                resource.validTo()
        ));

        if (promotion.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var promotionResource = PromotionResourceFromEntityAssembler.toResourceFromEntity(promotion.get());

        return new ResponseEntity<>(promotionResource, HttpStatus.CREATED);
    }

    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Deactivate a promotion")
    @ApiResponse(responseCode = "200", description = "Promotion deactivated successfully", content = @Content(mediaType = "application/json"))
    public ResponseEntity<?> deactivatePromotion(@PathVariable UUID promotionId) {
        parkingCommandService.handle(new DeactivatePromotionCommand(promotionId));

        return ResponseEntity.ok("Promotion deactivated successfully");
    }
}
