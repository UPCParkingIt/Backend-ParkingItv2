package com.parkingit.cloud.iam.interfaces.rest.transform;

import com.parkingit.cloud.iam.domain.model.commands.RecoverPasswordCommand;
import com.parkingit.cloud.iam.interfaces.rest.resources.RecoverPasswordResource;

public class RecoverPasswordCommandFromResourceAssembler {
    public static RecoverPasswordCommand toCommandFromResource(RecoverPasswordResource resource) {
        return new RecoverPasswordCommand(resource.email());
    }
}