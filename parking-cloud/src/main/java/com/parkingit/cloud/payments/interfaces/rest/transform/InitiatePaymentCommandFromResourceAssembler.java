package com.parkingit.cloud.payments.interfaces.rest.transform;

import com.parkingit.cloud.payments.domain.model.commands.InitiatePaymentCommand;
import com.parkingit.cloud.payments.interfaces.rest.resources.InitiatePaymentResource;

public class InitiatePaymentCommandFromResourceAssembler {
    public static InitiatePaymentCommand toCommandFromResource(InitiatePaymentResource resource) {
        return new InitiatePaymentCommand(
                resource.reservationId(),
                null,
                resource.amount(),
                resource.paymentMethod(),
                resource.description()
        );
    }
}
