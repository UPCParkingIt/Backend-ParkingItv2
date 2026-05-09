package com.parkingit.edge.config;

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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;

@Service
@Slf4j
public class SupabaseStorageService {
    private final S3Client s3Client;
    private final String bucketName;

    public SupabaseStorageService(
            @Value("${supabase.s3.endpoint}") String endpoint,
            @Value("${supabase.s3.region}") String region,
            @Value("${supabase.s3.access-key}") String accessKey,
            @Value("${supabase.s3.secret-key}") String secretKey,
            @Value("${supabase.s3.bucket}") String bucketName
    ) {
        this.bucketName = bucketName;

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    public String uploadImage(MultipartFile file, String path) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return String.format("https://aqblflmphqqnmhusszbh.supabase.co/storage/v1/object/public/%s/%s", bucketName, path);

        } catch (S3Exception e) {
            throw new RuntimeException("Error from Supabase S3: [" + e.awsErrorDetails().errorCode() + "] " + e.getMessage());
        } catch (SdkClientException e) {
            throw new RuntimeException("Error while connecting to the Storage: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Error while processing local file: " + e.getMessage());
        }
    }
}
