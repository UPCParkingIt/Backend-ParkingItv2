package com.parkingit.cloud.iam.domain.services;

import com.parkingit.cloud.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
  void handle(SeedRolesCommand command);
}
