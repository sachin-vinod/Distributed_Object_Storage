package com.example.distributed_object_storage.storage;

import com.example.distributed_object_storage.config.MinioConfig;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
public class MinioObjectStore implements ObjectStore {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStore.class);

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioObjectStore(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.bucketName = minioConfig.getBucketName();
    }

    @PostConstruct
    public void initBucket() {
        log.info("Connecting to MinIO to verify bucket: '{}'", bucketName);
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("MinIO bucket '{}' created successfully.", bucketName);
            } else {
                log.info("MinIO bucket '{}' already exists and is ready.", bucketName);
            }
        } catch (Exception exception) {
            log.warn("MinIO bucket '{}' could not be initialized at startup (MinIO might be offline): {}", 
                    bucketName, exception.getMessage());
        }
    }

    @Override
    public void putObject(String storageKey, byte[] content, String contentType) {
        byte[] data = content == null ? new byte[0] : content;
        String mimeType = contentType != null ? contentType : "application/octet-stream";
        log.debug("Writing to MinIO [bucket='{}', key='{}', size={} bytes, contentType='{}']",
                bucketName, storageKey, data.length, mimeType);
        
        long startTime = System.currentTimeMillis();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .stream(inputStream, data.length, -1)
                            .contentType(mimeType)
                            .build()
            );
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("Successfully stored object [key='{}'] in MinIO ({} bytes in {} ms)", 
                    storageKey, data.length, elapsed);
        } catch (Exception exception) {
            log.error("Failed storing object [key='{}'] in MinIO: {}", storageKey, exception.getMessage(), exception);
            throw new RuntimeException("Failed to store object in MinIO: " + exception.getMessage(), exception);
        }
    }

    @Override
    public byte[] getObject(String storageKey) {
        log.debug("Reading object from MinIO [bucket='{}', key='{}']", bucketName, storageKey);
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storageKey)
                        .build())) {
            byte[] bytes = stream.readAllBytes();
            log.info("Successfully retrieved {} bytes for key='{}' from MinIO", bytes.length, storageKey);
            return bytes;
        } catch (Exception exception) {
            log.error("Failed to get object [key='{}'] from MinIO: {}", storageKey, exception.getMessage());
            throw new RuntimeException("Failed to get object from MinIO: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean deleteObject(String storageKey) {
        log.info("Deleting object [key='{}'] from MinIO bucket '{}'", storageKey, bucketName);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            log.info("Successfully deleted object [key='{}'] from MinIO", storageKey);
            return true;
        } catch (Exception exception) {
            log.error("Failed to delete object [key='{}'] from MinIO: {}", storageKey, exception.getMessage());
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
            log.debug("Checked existence for [key='{}'] -> found: true", storageKey);
            return true;
        } catch (Exception exception) {
            log.debug("Checked existence for [key='{}'] -> found: false", storageKey);
            return false;
        }
    }
}
