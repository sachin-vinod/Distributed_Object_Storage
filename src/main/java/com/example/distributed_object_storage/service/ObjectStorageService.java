package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectMetadata;
import com.example.distributed_object_storage.dto.ObjectRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObjectStorageService {

    private final MetadataService metadataService;
    private final StorageService storageService;

    public ObjectStorageService(MetadataService metadataService, StorageService storageService) {
        this.metadataService = metadataService;
        this.storageService = storageService;
    }

    public ObjectMetadata createObject(String userId, ObjectRequest request) {
        String storageKey = userId + "/" + request.getName();
        storageService.storeObject(storageKey, request.getContent());
        return metadataService.createMetadata(request, storageKey);
    }

    public ObjectMetadata getObject(String objectId) {
        return metadataService.getMetadata(objectId);
    }

    public List<ObjectMetadata> getAllObjects() {
        return metadataService.getAllMetadata();
    }

    public ObjectMetadata updateObject(String objectId, ObjectRequest request) {
        ObjectMetadata existing = metadataService.getMetadata(objectId);
        if (existing == null) {
            return null;
        }

        metadataService.updateMetadata(objectId, request);
        storageService.storeObject(existing.getStorageKey(), request.getContent());
        return metadataService.getMetadata(objectId);
    }

    public boolean deleteObject(String objectId) {
        ObjectMetadata existing = metadataService.getMetadata(objectId);
        boolean metadataDeleted = metadataService.deleteMetadata(objectId);
        boolean storageDeleted = existing != null && storageService.deleteObject(existing.getStorageKey());
        return metadataDeleted || storageDeleted;
    }
}
