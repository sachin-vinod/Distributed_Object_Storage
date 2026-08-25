package com.example.distributed_object_storage.storage;

import com.example.distributed_object_storage.config.MinioConfig;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class MinioObjectStore implements ObjectStore {

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioObjectStore(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.bucketName = minioConfig.getBucketName();
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to initialize MinIO bucket: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void putObject(String storageKey, byte[] content, String contentType) {
        try {
            byte[] data = content == null ? new byte[0] : content;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageKey)
                                .stream(inputStream, data.length, -1)
                                .contentType(contentType != null ? contentType : "application/octet-stream")
                                .build()
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to store object in MinIO: " + exception.getMessage(), exception);
        }
    }

    @Override
    public byte[] getObject(String storageKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storageKey)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to get object from MinIO: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean deleteObject(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public boolean exists(String storageKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
