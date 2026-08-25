package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectResponse;
import com.example.distributed_object_storage.storage.ObjectStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

    private final ObjectStore objectStore;

    public ObjectStorageService(ObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    public ObjectResponse createObject(String userId, ObjectRequest request) {
        log.info("Processing createObject request: userId='{}', filename='{}', size={} bytes", 
                userId, request.getName(), request.getSize());

        String storageKey = userId + "/" + request.getName();
        log.info("Generated storageKey: '{}'", storageKey);

        objectStore.putObject(storageKey, request.getContent(), request.getContentType());
        log.info("Object '{}' successfully stored for user '{}'", request.getName(), userId);

        return new ObjectResponse(request.getName(), request.getContentType(), request.getSize());
    }

    public byte[] getObjectContent(String storageKey) {
        log.info("Processing getObject for storageKey='{}'", storageKey);
        return objectStore.getObject(storageKey);
    }

    public boolean deleteObject(String userId, String filename) {
        String storageKey = userId + "/" + filename;
        log.info("Processing deleteObject: userId='{}', filename='{}', storageKey='{}'", 
                userId, filename, storageKey);
        return objectStore.deleteObject(storageKey);
    }
}
