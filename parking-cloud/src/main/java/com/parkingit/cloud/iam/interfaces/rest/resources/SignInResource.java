package com.parkingit.cloud.iam.interfaces.rest.resources;

public record SignInResource(
        String email,
        String password
) {
}
