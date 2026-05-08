package com.parkingit.cloud.iam.interfaces.rest.transform;

import com.parkingit.cloud.iam.domain.model.commands.SignUpCommand;
import com.parkingit.cloud.iam.domain.model.entities.Role;
import com.parkingit.cloud.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;

public class SignUpCommandFromResourceAssembler {
  public static SignUpCommand toCommandFromResource(SignUpResource resource) {
    var roles = resource.roles() != null
        ? resource.roles().stream().map(Role::toRoleFromName).toList()
        : new ArrayList<Role>();
    return new SignUpCommand(
            resource.email(),
            resource.password(),
            resource.firstName(),
            resource.lastName(),
            resource.phoneNumber(),
            resource.dniNumber(),
            roles
    );
  }
}
