package com.parkingit.cloud.iam.interfaces.rest;

import com.parkingit.cloud.iam.domain.model.queries.GetAllUsersQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetUserByIdQuery;
import com.parkingit.cloud.iam.domain.services.UserQueryService;
import com.parkingit.cloud.iam.interfaces.rest.resources.UserResource;
import com.parkingit.cloud.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
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
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User Management Endpoints - Retrieve user information")
public class UsersController {
    private final UserQueryService userQueryService;

    /**
     * Retrieves a list of all users in the system.
     *
     * @return a ResponseEntity containing a list of all user resources
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a complete list of all registered users in the system")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserResource.class)))
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery());
        List<UserResource> resources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a specific user by their unique identifier.
     *
     * @param id the unique identifier of the user to retrieve
     * @return a ResponseEntity containing the user resource, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user's information using their unique identifier")
    @ApiResponse(responseCode = "200", description = "User found and returned successfully", content = @Content(schema = @Schema(implementation = UserResource.class)))
    @ApiResponse(responseCode = "404", description = "User not found with the provided ID")
    public ResponseEntity<UserResource> getUserById(@PathVariable UUID id) {
        var user = userQueryService.handle(new GetUserByIdQuery(id));
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var resource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(resource);
    }
}
