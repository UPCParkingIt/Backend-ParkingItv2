package com.parkingit.cloud.iam.interfaces.rest.resources;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record UserResource(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String dniNumber,
        List<String> roles,
        Date createdAt
) {
}
