package com.parkingit.cloud.iam.interfaces.rest;

import com.parkingit.cloud.iam.domain.exceptions.UserNotFoundException;
import com.parkingit.cloud.iam.domain.model.commands.AddCompanionCommand;
import com.parkingit.cloud.iam.domain.model.commands.RemoveCompanionCommand;
import com.parkingit.cloud.iam.domain.services.CompanionCommandService;
import com.parkingit.cloud.iam.domain.services.UserCommandService;
import com.parkingit.cloud.iam.interfaces.rest.resources.*;
import com.parkingit.cloud.iam.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication and Authorization Endpoints - User sign-in, sign-up, password recovery, and companion management")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final CompanionCommandService companionCommandService;

    /**
     * Authenticates a user by email and password.
     *
     * @param signInResource the sign-in request containing email and password
     * @return an AuthenticatedUserResource containing user data and JWT token
     */
    @PostMapping("/sign-in")
    @Operation(summary = "User sign-in", description = "Authenticates a user with email and password. Returns user data and JWT bearer token for subsequent API calls")
    @ApiResponse(responseCode = "200", description = "Authentication successful, returns user and token", content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class)))
    @ApiResponse(responseCode = "404", description = "User not found or invalid credentials")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);

        var authenticatedUser = userCommandService.handle(signInCommand);

        if (authenticatedUser.isEmpty()) { return ResponseEntity.notFound().build(); }

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
            .toResourceFromEntity(
                authenticatedUser.get().getLeft(), authenticatedUser.get().getRight());

        return ResponseEntity.ok(authenticatedUserResource);
    }

    /**
     * Creates a new user account with email, password, and personal information.
     *
     * @param signUpResource the sign-up request containing user details
     * @return the created UserResource with status 201
     */
    @PostMapping("/sign-up")
    @Operation(summary = "User registration", description = "Creates a new user account with email, password, name, DNI, and phone number. User role (USER_ROLE) is assigned by default")
    @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResource.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data or email already exists")
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * Initiates a password recovery process by sending a reset link to the user's email.
     *
     * @param recoverPasswordResource the request containing the user's email
     * @return a PasswordRecoveryResponseResource with status message
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password - Request recovery", description = "Sends a password recovery link to the user's registered email address. Link expires in 24 hours")
    @ApiResponse(responseCode = "200", description = "Recovery email sent if account exists (security: always return success)")
    public ResponseEntity<PasswordRecoveryResponseResource> forgotPassword(
            @RequestBody RecoverPasswordResource recoverPasswordResource
    ) {
        try {
            var recoverCommand = RecoverPasswordCommandFromResourceAssembler.toCommandFromResource(recoverPasswordResource);

            userCommandService.handle(recoverCommand);

            var response = new PasswordRecoveryResponseResource(
                    "Si el email existe en nuestro sistema, recibirás un enlace de recuperación en tu bandeja de entrada.",
                    true
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            var errorResponse = new PasswordRecoveryResponseResource(
                    "Error procesando la recuperación de contraseña",
                    false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Resets a user's password using a reset token received via email.
     *
     * @param resetPasswordResource the request containing token, new password, and confirmation
     * @return a PasswordRecoveryResponseResource with status message
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password - Complete recovery", description = "Completes the password reset process using the token sent to email and new password")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired reset token")
    @ApiResponse(responseCode = "500", description = "Error resetting password")
    public ResponseEntity<PasswordRecoveryResponseResource> resetPassword(
            @RequestBody ResetPasswordResource resetPasswordResource
    ) {
        try {
            var resetCommand = ResetPasswordCommandFromResourceAssembler
                    .toCommandFromResource(resetPasswordResource);

            var updatedUser = userCommandService.handle(resetCommand);

            if (updatedUser.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new PasswordRecoveryResponseResource("Error al reestablecer la contraseña", false)
                );
            }

            var response = new PasswordRecoveryResponseResource(
                    "Contraseña reestablecida exitosamente. Ya puedes iniciar sesión.",
                    true
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            var errorResponse = new PasswordRecoveryResponseResource(
                    "Solicitud inválida: " + e.getMessage(),
                    false
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            var errorResponse = new PasswordRecoveryResponseResource(
                    "Error al reestablecer la contraseña: " + e.getMessage(),
                    false
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Adds a companion (authorized user) to the authenticated user's account.
     * Stores companion's facial data for verification during parking exit.
     * Maximum 5 companions per user.
     *
     * @param resource the request containing companion name and facial image
     * @param userDetails the authenticated user's details
     * @return success message or error
     */
    @PostMapping("/companions")
    @Operation(summary = "Add companion to user account",
               description = "Adds a companion with facial recognition data. Used to verify authorized users on parking exit. Max 5 companions per user")
    @ApiResponse(responseCode = "200", description = "Companion added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid companion data or maximum companions reached")
    public ResponseEntity<?> addCompanion(@RequestBody UploadCompanionResource resource, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var email = userDetails.getUsername();

            companionCommandService.handle(email, new AddCompanionCommand(resource.companionName(), resource.faceImage()));

            return ResponseEntity.ok("Companion added successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Removes a companion from the authenticated user's account and deletes their facial data.
     *
     * @param companionId the ID of the companion to remove
     * @param userDetails the authenticated user's details
     * @return success message or error
     */
    @DeleteMapping("/companions/{companionId}")
    @Operation(summary = "Remove companion from user account",
               description = "Deletes a companion and removes their facial recognition data from the system")
    @ApiResponse(responseCode = "200", description = "Companion removed successfully")
    @ApiResponse(responseCode = "404", description = "Companion not found")
    public ResponseEntity<?> removeCompanion(@PathVariable UUID companionId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var email = userDetails.getUsername();

            companionCommandService.handle(new RemoveCompanionCommand(email, companionId));

            return ResponseEntity.ok("Companion removed successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
