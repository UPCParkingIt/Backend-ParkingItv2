package com.parkingit.cloud.iam.domain.services;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.commands.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public interface UserCommandService {
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);
    Optional<User> handle(SignUpCommand command);
    void handle(RecoverPasswordCommand command);
    Optional<User> handle(ResetPasswordCommand command);
}
