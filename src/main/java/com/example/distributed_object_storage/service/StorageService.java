package com.example.distributed_object_storage.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {

    private final Path storageRoot;

    public StorageService() {
        this(Paths.get("storage").toAbsolutePath().normalize());
    }

    StorageService(Path storageRoot) {
        this.storageRoot = storageRoot;
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to initialize storage directory: " + exception.getMessage(), exception);
        }
    }

    public void storeObject(String storageKey, byte[] content) {
        Path targetPath = resolvePath(storageKey);
        try {
            Files.write(targetPath, content == null ? new byte[0] : content);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to store file: " + exception.getMessage(), exception);
        }
    }

    public boolean deleteObject(String storageKey) {
        try {
            return Files.deleteIfExists(resolvePath(storageKey));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to delete file: " + exception.getMessage(), exception);
        }
    }

    private Path resolvePath(String objectId) {
        return storageRoot.resolve(objectId).normalize();
    }
}
