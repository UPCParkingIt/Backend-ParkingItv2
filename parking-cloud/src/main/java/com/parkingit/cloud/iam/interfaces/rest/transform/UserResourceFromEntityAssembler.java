package com.parkingit.cloud.iam.interfaces.rest.transform;


import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.entities.Role;
import com.parkingit.cloud.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        var roles = user.getRoles().stream()
                .map(Role::getStringName)
                .toList();

        return new UserResource(
                user.getId(),
                user.getEmail().getValue(),
                user.getPersonName().getFirstName(),
                user.getPersonName().getLastName(),
                user.getPhoneNumber().getValue(),
                user.getDni().getValue(),
                roles,
                user.getCreatedAt()
        );
    }
}
