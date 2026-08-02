package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectMetadata;
import com.example.distributed_object_storage.dto.ObjectRequest;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetadataService {

    private final Map<String, ObjectMetadata> objectStore = new HashMap<>();

    public ObjectMetadata createMetadata(ObjectRequest request, String storageKey) {
        String objectId = generateObjectId(request.getName());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setId(objectId);
        metadata.setName(request.getName());
        metadata.setContentType(request.getContentType());
        metadata.setSize(request.getSize());
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setUpdatedAt(LocalDateTime.now());
        metadata.setStatus("ACTIVE");
        metadata.setStorageKey(storageKey);

        objectStore.put(objectId, metadata);
        return metadata;
    }

    private String generateObjectId(String filename) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(filename.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("SHA-256 algorithm not available: " + exception.getMessage(), exception);
        }
    }

    public ObjectMetadata getMetadata(String objectId) {
        return objectStore.get(objectId);
    }

    public List<ObjectMetadata> getAllMetadata() {
        return new ArrayList<>(objectStore.values());
    }

    public ObjectMetadata updateMetadata(String objectId, ObjectRequest request) {
        ObjectMetadata existing = objectStore.get(objectId);
        if (existing == null) {
            return null;
        }

        existing.setName(request.getName());
        existing.setContentType(request.getContentType());
        existing.setSize(request.getSize());
        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }

    public boolean deleteMetadata(String objectId) {
        return objectStore.remove(objectId) != null;
    }
}
