package com.parkingit.cloud.iam.interfaces.rest.transform;

import com.parkingit.cloud.iam.domain.model.commands.ResetPasswordCommand;
import com.parkingit.cloud.iam.interfaces.rest.resources.ResetPasswordResource;

public class ResetPasswordCommandFromResourceAssembler {
    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(
                resource.token(),
                resource.newPassword()
        );
    }
}
