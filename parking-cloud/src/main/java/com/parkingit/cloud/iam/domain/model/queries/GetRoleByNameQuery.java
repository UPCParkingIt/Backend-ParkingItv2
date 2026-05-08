package com.parkingit.cloud.iam.domain.model.queries;

import com.parkingit.cloud.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}