package com.parkingit.cloud.iam.domain.model.commands;

import java.util.UUID;

public record RemoveCompanionCommand(
        String email,
        UUID companionId
) {
}
