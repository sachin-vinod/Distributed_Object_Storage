package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectMetadata;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ObjectStorageService {

    private final Map<String, ObjectMetadata> objectStore = new HashMap<>();
    private static final String STORAGE_DIR = "storage";

    private String generateObjectId(String filename) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(filename.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available: " + e.getMessage());
        }
    }

    public ObjectMetadata createObject(ObjectRequest request) {
        String objectId = generateObjectId(request.getName());
        
        // Store actual file to filesystem
        try {
            File storageFolder = new File(STORAGE_DIR);
            if (!storageFolder.exists()) {
                storageFolder.mkdir();
            }
            
            File file = new File(STORAGE_DIR + File.separator + objectId);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(request.getContent());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
        
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setId(objectId);
        metadata.setName(request.getName());
        metadata.setContentType(request.getContentType());
        metadata.setSize(request.getSize());
        metadata.setCreatedAt(LocalDateTime.now());
        metadata.setUpdatedAt(LocalDateTime.now());
        metadata.setStatus("ACTIVE");

        objectStore.put(objectId, metadata);
        return metadata;
    }

    public ObjectMetadata getObject(String objectId) {
        return objectStore.get(objectId);
    }

    public List<ObjectMetadata> getAllObjects() {
        return new ArrayList<>(objectStore.values());
    }

    public ObjectMetadata updateObject(String objectId, ObjectRequest request) {
        ObjectMetadata existing = objectStore.get(objectId);
        if (existing != null) {
            existing.setName(request.getName());
            existing.setContentType(request.getContentType());
            existing.setSize(request.getSize());
            existing.setUpdatedAt(LocalDateTime.now());
        }
        return existing;
    }

    public boolean deleteObject(String objectId) {
        return objectStore.remove(objectId) != null;
    }
}
