package com.parkingit.cloud.iam.infrastructure.supabase.s3;

import org.springframework.web.multipart.MultipartFile;

public interface SupabaseS3Service {
    String uploadFile(MultipartFile file, String keyPath);
    void deleteFile(String keyPath);
    String getFileUrl(String keyPath);
}
