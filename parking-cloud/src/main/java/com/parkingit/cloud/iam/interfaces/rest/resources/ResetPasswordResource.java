package com.parkingit.cloud.iam.interfaces.rest.resources;

public record ResetPasswordResource(
        String token,
        String newPassword
) {
}
