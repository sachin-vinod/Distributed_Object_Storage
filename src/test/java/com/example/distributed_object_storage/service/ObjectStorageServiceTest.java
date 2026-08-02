package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectMetadata;
import com.example.distributed_object_storage.dto.ObjectRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createUpdateAndDeleteObjectAcrossMetadataAndStorageServices() throws IOException {
        StorageService storageService = new StorageService(tempDir);
        MetadataService metadataService = new MetadataService();
        ObjectStorageService objectStorageService = new ObjectStorageService(metadataService, storageService);

        ObjectRequest createRequest = new ObjectRequest();
        createRequest.setName("hello.txt");
        createRequest.setContent("hello".getBytes(StandardCharsets.UTF_8));
        createRequest.setContentType("text/plain");
        createRequest.setSize(5);

        ObjectMetadata created = objectStorageService.createObject("user-1", createRequest);

        assertNotNull(created);
        assertEquals("ACTIVE", created.getStatus());
        assertEquals("user-1/hello.txt", created.getStorageKey());
        assertNotNull(created.getId());
        assertTrue(Files.exists(tempDir.resolve(created.getStorageKey())));
        assertNotNull(objectStorageService.getObject(created.getId()));

        ObjectRequest updateRequest = new ObjectRequest();
        updateRequest.setName("updated.txt");
        updateRequest.setContent("updated".getBytes(StandardCharsets.UTF_8));
        updateRequest.setContentType("text/plain");
        updateRequest.setSize(7);

        ObjectMetadata updated = objectStorageService.updateObject(created.getId(), updateRequest);

        assertNotNull(updated);
        assertEquals("updated.txt", updated.getName());
        assertTrue(Files.exists(tempDir.resolve(created.getStorageKey())));
        assertEquals("updated", Files.readString(tempDir.resolve(created.getStorageKey()), StandardCharsets.UTF_8));

        assertTrue(objectStorageService.deleteObject(created.getId()));
        assertFalse(Files.exists(tempDir.resolve(created.getStorageKey())));
        assertNull(objectStorageService.getObject(created.getId()));
    }
}
