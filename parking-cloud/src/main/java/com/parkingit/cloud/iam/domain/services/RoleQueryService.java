package com.parkingit.cloud.iam.domain.services;

import com.parkingit.cloud.iam.domain.model.entities.Role;
import com.parkingit.cloud.iam.domain.model.queries.GetAllRolesQuery;
import com.parkingit.cloud.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {
  List<Role> handle(GetAllRolesQuery query);
  Optional<Role> handle(GetRoleByNameQuery query);
}
