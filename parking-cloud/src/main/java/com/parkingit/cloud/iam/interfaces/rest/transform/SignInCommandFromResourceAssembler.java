package com.parkingit.cloud.iam.interfaces.rest.transform;


import com.parkingit.cloud.iam.domain.model.commands.SignInCommand;
import com.parkingit.cloud.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
  public static SignInCommand toCommandFromResource(SignInResource signInResource) {
    return new SignInCommand(signInResource.email(), signInResource.password());
  }
}
