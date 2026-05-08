package com.parkingit.cloud.iam.domain.model.commands;

public record ResetPasswordCommand(String token, String newPassword) {
}
