package com.parkingit.cloud.iam.interfaces.rest.resources;

public record CreateUserResource(
        String email,
        String passwordHash
) {
}
