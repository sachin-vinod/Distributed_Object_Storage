package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.config.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class StorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public StorageService(MinioClient minioClient, MinioConfig minioConfig) {
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

    public void storeObject(String storageKey, byte[] content) {
        try {
            byte[] data = content == null ? new byte[0] : content;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(storageKey)
                                .stream(inputStream, data.length, -1)
                                .build()
                );
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to store file in MinIO: " + exception.getMessage(), exception);
        }
    }

    public byte[] getObjectContent(String storageKey) {
        return null;
    }

    public boolean deleteObject(String storageKey) {
        return false;
    }
}
