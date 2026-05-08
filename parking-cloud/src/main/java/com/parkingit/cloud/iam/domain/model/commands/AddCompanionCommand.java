package com.parkingit.cloud.iam.domain.model.commands;

import org.springframework.web.multipart.MultipartFile;

public record AddCompanionCommand(
        String companionName,
        MultipartFile faceImage
) {
}
