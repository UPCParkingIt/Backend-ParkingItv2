package com.parkingit.cloud.payments.interfaces.rest.transform;

import com.parkingit.cloud.payments.domain.model.commands.InitiateExitPaymentCommand;
import com.parkingit.cloud.payments.interfaces.rest.resources.InitiateExitPaymentResource;

public class InitiateExitPaymentCommandFromResourceAssembler {
    public static InitiateExitPaymentCommand toCommandFromResource(InitiateExitPaymentResource resource) {
        return new InitiateExitPaymentCommand(
                resource.reservationId(),
                resource.parkingLogId(),
                resource.amount(),
                resource.paymentMethod(),
                resource.description()
        );
    }
}
