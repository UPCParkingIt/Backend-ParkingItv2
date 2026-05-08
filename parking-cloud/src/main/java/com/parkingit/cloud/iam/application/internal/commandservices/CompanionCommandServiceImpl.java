package com.parkingit.cloud.iam.application.internal.commandservices;

import com.parkingit.cloud.iam.domain.exceptions.UserNotFoundException;
import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.commands.AddCompanionCommand;
import com.parkingit.cloud.iam.domain.model.commands.RemoveCompanionCommand;
import com.parkingit.cloud.iam.domain.model.entities.UserCompanion;
import com.parkingit.cloud.iam.domain.model.valueobjects.PersonName;
import com.parkingit.cloud.iam.domain.services.CompanionCommandService;
import com.parkingit.cloud.iam.infrastructure.ml.MLService;
import com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories.UserCompanionRepository;
import com.parkingit.cloud.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.parkingit.cloud.iam.infrastructure.supabase.s3.SupabaseS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompanionCommandServiceImpl implements CompanionCommandService {
    private final UserRepository userRepository;
    private final UserCompanionRepository companionRepository;
    private final SupabaseS3Service s3Service;
    private final MLService mlService;

    @Override
    public void handle(String email, AddCompanionCommand command) {
        try {
            log.info("Adding companion for user: {}", email);

            User user = userRepository.findByEmail_Value(email).orElseThrow(() -> new UserNotFoundException("User not found: " + email));

            user.validateForOperation();

            if (!mlService.isValidFace(command.faceImage().getInputStream())) {
                throw new IllegalArgumentException("Face image does not contain a valid face");
            }

            String faceImageS3Url = s3Service.uploadFile(
                    command.faceImage(),
                    "companions/" + email + "/face"
            );

            String facialEmbedding = mlService.extractFacialEmbedding(
                    command.faceImage().getInputStream()
            );

            String[] nameParts = command.companionName().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            PersonName companionName = new PersonName(firstName, lastName);

            UserCompanion companion = UserCompanion.create(
                    user,
                    companionName,
                    facialEmbedding
            );

            companion.setFaceImage(faceImageS3Url);

            user.addCompanion(companion);

            userRepository.save(user);
            log.info("[CompanionCommandService] Companion added successfully for user: {}", email);
        } catch (IOException e) {
            log.error("[CompanionCommandService] Error processing companion images", e);
            throw new RuntimeException("Error processing companion images", e);
        }
    }

    @Override
    public void handle(RemoveCompanionCommand command) {
        log.info("[CompanionCommandService] Removing companion: {} for user: {}", command.companionId(), command.email());

        User user = userRepository.findByEmail_Value(command.email()).orElseThrow(() -> new UserNotFoundException("User not found: " + command.email()));

        UserCompanion companion = companionRepository.findById(command.companionId()).orElseThrow(() -> new UserNotFoundException("Companion not found: " + command.companionId()));

        if (!companion.getUser().getEmail().getValue().equals(command.email())) {
            throw new IllegalArgumentException("Companion does not belong to user");
        }

        if (companion.getFaceImageS3Url() != null) {
            try {
                String key = extractS3KeyFromUrl(companion.getFaceImageS3Url());
                s3Service.deleteFile(key);
                log.info("Face image deleted from S3");
            } catch (Exception e) {
                log.warn("Failed to delete face image from S3: {}", e.getMessage());
            }
        }

        user.removeCompanion(companion);
        userRepository.save(user);
        log.info("[CompanionCommandService] Companion removed successfully");
    }

    private String extractS3KeyFromUrl(String url) {
        try {
            String[] parts = url.split("/object/public/");
            if (parts.length > 1) {
                return parts[1].substring(parts[1].indexOf("/") + 1);
            }
        } catch (Exception e) {
            log.warn("Failed to extract S3 key from URL: {}", url);
        }
        return null;
    }
}
