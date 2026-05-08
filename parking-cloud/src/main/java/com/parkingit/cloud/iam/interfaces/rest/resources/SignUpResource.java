package com.parkingit.cloud.iam.interfaces.rest.resources;

import java.util.List;

public record SignUpResource(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String dniNumber,
        List<String> roles
) {
}

