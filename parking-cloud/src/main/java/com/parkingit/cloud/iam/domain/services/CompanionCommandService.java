package com.parkingit.cloud.iam.domain.services;

import com.parkingit.cloud.iam.domain.model.commands.AddCompanionCommand;
import com.parkingit.cloud.iam.domain.model.commands.RemoveCompanionCommand;

import java.util.UUID;

public interface CompanionCommandService {
    void handle(String email, AddCompanionCommand command);
    void handle(RemoveCompanionCommand command);
}
