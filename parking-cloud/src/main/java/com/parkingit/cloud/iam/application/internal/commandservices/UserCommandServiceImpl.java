package com.parkingit.cloud.iam.application.internal.commandservices;

import com.parkingit.cloud.iam.application.internal.outboundservices.acl.ExternalNotificationService;
import com.parkingit.cloud.iam.application.internal.outboundservices.hashing.HashingService;
import com.parkingit.cloud.iam.application.internal.outboundservices.tokens.TokenService;
import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.commands.*;
import com.parkingit.cloud.iam.domain.model.entities.PasswordResetToken;
import com.parkingit.cloud.iam.domain.model.events.UserCreatedEvent;
import com.parkingit.cloud.iam.domain.model.valueobjects.*;
import com.parkingit.cloud.iam.domain.services.UserCommandService;
import com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories.PasswordResetTokenRepository;
import com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ExternalNotificationService externalNotificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        try {
            var existingUser = userRepository.findByEmail_Value(command.email());
            if (existingUser.isEmpty()) {
                log.warn("[UserCommandService] Sign-in attempt with non-existent email: {}", command.email());
                throw new RuntimeException("Email not found");
            }

            if (!hashingService.matches(command.password(), existingUser.get().getPasswordHash())) {
                log.warn("[UserCommandService] Invalid password for user: {}", command.email());
                throw new RuntimeException("Invalid password");
            }

            var token = tokenService.generateToken(existingUser.get().getUsername());
            log.info("[UserCommandService] User signed in successfully: {}", command.email());
            return Optional.of(ImmutablePair.of(existingUser.get(), token));
        } catch (Exception e) {
            log.error("[UserCommandService] Error during sign-in: {}", e.getMessage(), e);
            throw new RuntimeException("Error during sign-in: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        try {
            if (userRepository.existsByEmail_Value(command.email())) {
                log.warn("[UserCommandService] Sign-up attempt with existing email: {}", command.email());
                throw new RuntimeException("Email already exists");
            }

            var roles = command.roles().stream()
                    .map(role -> roleRepository.findByName(role.getName())
                            .orElseThrow(() -> new RuntimeException("Role name not found")))
                    .toList();

            Email email = new Email(command.email());
            HashedPassword password = new HashedPassword(
                    hashingService.encode(command.password())
            );
            PersonName personName = new PersonName(
                    command.firstName(),
                    command.lastName()
            );
            DNI dni = new DNI(command.dniNumber());
            PhoneNumber phoneNumber = command.phoneNumber() != null && !command.phoneNumber().isEmpty()
                    ? new PhoneNumber(command.phoneNumber())
                    : null;

            User newUser = User.createRegisteredUser(
                    email,
                    password,
                    personName,
                    dni,
                    phoneNumber,
                    roles
            );

            var savedUser = userRepository.save(newUser);

            eventPublisher.publishEvent(new UserCreatedEvent(savedUser, savedUser.getId()));

            log.info("[UserCommandService] User created successfully with email: {}", command.email());
            return userRepository.findByEmail_Value(command.email());
        } catch (Exception e) {
            log.error("[UserCommandService] Error during sign-up: {}", e.getMessage(), e);
            throw new RuntimeException("Error during sign-up: " + e.getMessage(), e);
        }
    }

    @Override
    public void handle(RecoverPasswordCommand command) {
        try {
            log.info("[UserCommandService] Processing password recovery for email: {}", command.email());

            var existingUser = userRepository.findByEmail_Value(command.email());
            if (existingUser.isEmpty()) {
                log.warn("[UserCommandService] Password recovery attempt for non-existent email: {}", command.email());
                return;
            }

            String recoveryToken = UUID.randomUUID().toString();

            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
            PasswordResetToken passwordResetToken = new PasswordResetToken(
                    recoveryToken,
                    existingUser.get(),
                    expiresAt
            );

            passwordResetTokenRepository.save(passwordResetToken);
            log.info("[UserCommandService] Password reset token saved for user: {}", command.email());

            User user = existingUser.get();
            String firstName = user.getPersonName() != null
                    ? user.getPersonName().getFirstName()
                    : "";
            String emailValue = user.getEmail() != null
                    ? user.getEmail().getValue()
                    : "";

            externalNotificationService.sendPasswordRecoveryEmail(
                    user,
                    firstName,
                    emailValue,
                    recoveryToken
            );

            log.info("[UserCommandService] Password recovery process completed for: {}", command.email());

        } catch (Exception e) {
            log.error("[UserCommandService] Error processing password recovery: {}", e.getMessage(), e);
            throw new RuntimeException("Error while processing password recovery: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> handle(ResetPasswordCommand command) {
        try {
            log.info("[UserCommandService] Processing password reset with token");

            var existingToken = passwordResetTokenRepository.findByToken(command.token());
            if (existingToken.isEmpty()) {
                log.warn("[UserCommandService] Invalid recovery token used");
                throw new IllegalArgumentException("Invalid recovery token");
            }

            PasswordResetToken resetToken = existingToken.get();

            if (!resetToken.isValid()) {
                log.warn("[UserCommandService] Expired or already used recovery token");
                throw new IllegalArgumentException("Recovery token has expired or has already been used");
            }

            if (command.newPassword() == null || command.newPassword().isBlank()) {
                throw new IllegalArgumentException("New password cannot be empty");
            }

            User user = resetToken.getUser();

            HashedPassword newHashedPassword = new HashedPassword(
                    hashingService.encode(command.newPassword())
            );

            user.setPassword(newHashedPassword);

            var savedUser = userRepository.save(user);

            resetToken.setIsUsed(true);
            passwordResetTokenRepository.save(resetToken);
            log.info("[UserCommandService] Token marked as used");

            String firstName = user.getPersonName() != null
                    ? user.getPersonName().getFirstName()
                    : "";
            String emailValue = user.getEmail() != null
                    ? user.getEmail().getValue()
                    : "";

            externalNotificationService.sendPasswordResetSuccessEmail(
                    user,
                    firstName,
                    emailValue
            );

            log.info("[UserCommandService] Password reset successfully for user: {}", user.getEmail());
            return Optional.of(savedUser);

        } catch (Exception e) {
            log.error("[UserCommandService] Error resetting password: {}", e.getMessage(), e);
            throw new RuntimeException("Error while resetting password: " + e.getMessage(), e);
        }
    }
}
