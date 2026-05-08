package com.parkingit.cloud.iam.interfaces.rest.resources;

import org.springframework.web.multipart.MultipartFile;

public record UploadCompanionResource(
        String companionName,
        MultipartFile faceImage
) {
}
