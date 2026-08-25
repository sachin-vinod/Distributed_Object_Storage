package com.example.distributed_object_storage.service;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectResponse;
import com.example.distributed_object_storage.storage.ObjectStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObjectStorageServiceTest {

    private ObjectStorageService objectStorageService;
    private InMemoryObjectStore inMemoryObjectStore;

    static class InMemoryObjectStore implements ObjectStore {
        private final Map<String, byte[]> data = new HashMap<>();

        @Override
        public void putObject(String storageKey, byte[] content, String contentType) {
            data.put(storageKey, content);
        }

        @Override
        public byte[] getObject(String storageKey) {
            return data.get(storageKey);
        }

        @Override
        public boolean deleteObject(String storageKey) {
            return data.remove(storageKey) != null;
        }

        @Override
        public boolean exists(String storageKey) {
            return data.containsKey(storageKey);
        }
    }

    @BeforeEach
    void setUp() {
        inMemoryObjectStore = new InMemoryObjectStore();
        objectStorageService = new ObjectStorageService(inMemoryObjectStore);
    }

    @Test
    void testCreateObjectStoresCorrectly() {
        ObjectRequest request = new ObjectRequest();
        request.setName("test.txt");
        request.setContentType("text/plain");
        request.setContent("hello distributed storage".getBytes(StandardCharsets.UTF_8));
        request.setSize((long) request.getContent().length);

        ObjectResponse response = objectStorageService.createObject("user123", request);

        assertNotNull(response);
        assertEquals("test.txt", response.getFilename());
        assertEquals("text/plain", response.getContentType());
        assertTrue(inMemoryObjectStore.exists("user123/test.txt"));
        assertArrayEquals(request.getContent(), objectStorageService.getObjectContent("user123/test.txt"));
    }

    @Test
    void testDeleteObject() {
        inMemoryObjectStore.putObject("user123/delete-me.txt", "data".getBytes(StandardCharsets.UTF_8), "text/plain");
        assertTrue(inMemoryObjectStore.exists("user123/delete-me.txt"));

        boolean deleted = objectStorageService.deleteObject("user123", "delete-me.txt");
        assertTrue(deleted);
        assertFalse(inMemoryObjectStore.exists("user123/delete-me.txt"));
    }
}
