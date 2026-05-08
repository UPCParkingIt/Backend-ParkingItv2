package com.parkingit.cloud.iam.infrastructure.supabase.s3.services;

import com.parkingit.cloud.iam.infrastructure.supabase.s3.SupabaseS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;

@SuppressWarnings("LoggingSimilarMessage")
@Service
@Slf4j
public class SupabaseS3ServiceImpl implements SupabaseS3Service {
    private final S3Client s3Client;
    private final String bucketName;
    private final String supabaseUrl;
    private final String supabaseProjectId;

    public SupabaseS3ServiceImpl(
            @Value("${supabase.s3.endpoint}") String endpoint,
            @Value("${supabase.s3.region}") String region,
            @Value("${supabase.s3.access-key}") String accessKey,
            @Value("${supabase.s3.secret-key}") String secretKey,
            @Value("${supabase.s3.bucket}") String bucketName,
            @Value("${supabase.url}") String supabaseUrl
    ) {
        this.bucketName = bucketName;
        this.supabaseUrl = supabaseUrl;
        this.supabaseProjectId = extractProjectId(supabaseUrl);

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        log.info("Supabase S3 Client initialized - Bucket: {}, Region: {}", bucketName, region);
    }

    @Override
    public String uploadFile(MultipartFile file, String keyPath) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            if (keyPath == null || keyPath.isEmpty()) {
                throw new IllegalArgumentException("Key path cannot be empty");
            }

            log.debug("Uploading file to S3: bucket={}, path={}, size={}", bucketName, keyPath, file.getSize());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyPath)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = String.format(
                    "%s/storage/v1/object/public/%s/%s",
                    supabaseUrl,
                    bucketName,
                    keyPath
            );

            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (S3Exception e) {
            log.error("S3 Error: [{}] {}", e.awsErrorDetails().errorCode(), e.getMessage());
            throw new RuntimeException("Error uploading to Supabase S3: [" + e.awsErrorDetails().errorCode() + "] " + e.getMessage(), e);

        } catch (SdkClientException e) {
            log.error("AWS SDK Client Error: {}", e.getMessage());
            throw new RuntimeException("Error connecting to Supabase Storage: " + e.getMessage(), e);

        } catch (IOException e) {
            log.error("IO Error: {}", e.getMessage());
            throw new RuntimeException("Error processing file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String keyPath) {
        try {
            if (keyPath == null || keyPath.isEmpty()) {
                throw new IllegalArgumentException("Key path cannot be empty");
            }

            log.debug("Deleting file from S3: bucket={}, path={}", bucketName, keyPath);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyPath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("File deleted successfully: {}", keyPath);

        } catch (S3Exception e) {
            log.error("S3 Error deleting file: [{}] {}", e.awsErrorDetails().errorCode(), e.getMessage());
            throw new RuntimeException("Error deleting from Supabase S3: " + e.getMessage(), e);

        } catch (SdkClientException e) {
            log.error("AWS SDK Client Error: {}", e.getMessage());
            throw new RuntimeException("Error connecting to Supabase Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String keyPath) {
        if (keyPath == null || keyPath.isEmpty()) {
            throw new IllegalArgumentException("Key path cannot be empty");
        }

        return String.format(
                "%s/storage/v1/object/public/%s/%s",
                supabaseUrl,
                bucketName,
                keyPath
        );
    }

    private String extractProjectId(String supabaseUrl) {
        try {
            return supabaseUrl.split("\\.")[0].replace("https://", "");
        } catch (Exception e) {
            log.warn("Could not extract project ID from Supabase URL: {}", supabaseUrl);
            return "";
        }
    }
}
