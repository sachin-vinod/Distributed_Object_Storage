package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectResponse;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageService {

    private final StorageService storageService;

    public ObjectStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public ObjectResponse createObject(String userId, ObjectRequest request) {
        String storageKey = userId + "/" + request.getName();
        storageService.storeObject(storageKey, request.getContent());
        
        return new ObjectResponse(request.getName(), request.getContentType(), request.getSize());
    }

    public byte[] getObjectContent(String storageKey) {
        return storageService.getObjectContent(storageKey);
    }

    public boolean deleteObject(String userId, String filename) {
        String storageKey = userId + "/" + filename;
        return storageService.deleteObject(storageKey);
    }
}
