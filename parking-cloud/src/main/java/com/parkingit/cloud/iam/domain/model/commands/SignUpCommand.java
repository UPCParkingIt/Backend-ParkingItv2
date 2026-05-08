package com.parkingit.cloud.iam.domain.model.commands;

import com.parkingit.cloud.iam.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String dniNumber,
        List<Role> roles
) {
}
