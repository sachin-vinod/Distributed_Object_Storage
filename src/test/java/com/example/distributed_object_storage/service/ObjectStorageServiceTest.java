package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectResponse;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void createAndDeleteObject() throws IOException {
        StorageService storageService = new StorageService(tempDir);
        ObjectStorageService objectStorageService = new ObjectStorageService(storageService);

        ObjectRequest createRequest = new ObjectRequest();
        createRequest.setName("hello.txt");
        createRequest.setContent("hello".getBytes(StandardCharsets.UTF_8));
        createRequest.setContentType("text/plain");
        createRequest.setSize(5);

        ObjectResponse created = objectStorageService.createObject("user-1", createRequest);

        assertNotNull(created);
        assertEquals("hello.txt", created.getFilename());
        assertEquals("text/plain", created.getContentType());
        assertEquals(5, created.getSize());

        String storageKey = "user-1/hello.txt";
        assertTrue(Files.exists(tempDir.resolve(storageKey)));
        assertEquals("hello", Files.readString(tempDir.resolve(storageKey), StandardCharsets.UTF_8));

        assertTrue(objectStorageService.deleteObject("user-1", "hello.txt"));
        assertFalse(Files.exists(tempDir.resolve(storageKey)));
    }
}
