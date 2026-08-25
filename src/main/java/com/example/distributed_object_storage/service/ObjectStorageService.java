package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectResponse;
import com.example.distributed_object_storage.storage.ObjectStore;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageService {

    private final ObjectStore objectStore;

    public ObjectStorageService(ObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public ObjectResponse createObject(String userId, ObjectRequest request) {
        String storageKey = userId + "/" + request.getName();
        objectStore.putObject(storageKey, request.getContent(), request.getContentType());

        return new ObjectResponse(request.getName(), request.getContentType(), request.getSize());
    }

    public byte[] getObjectContent(String storageKey) {
        return objectStore.getObject(storageKey);
    }

    public boolean deleteObject(String userId, String filename) {
        String storageKey = userId + "/" + filename;
        return objectStore.deleteObject(storageKey);
    }
}
