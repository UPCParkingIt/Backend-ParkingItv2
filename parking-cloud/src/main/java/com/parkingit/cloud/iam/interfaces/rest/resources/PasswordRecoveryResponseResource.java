package com.parkingit.cloud.iam.interfaces.rest.resources;

public record PasswordRecoveryResponseResource(
        String message,
        Boolean success
) {
}
