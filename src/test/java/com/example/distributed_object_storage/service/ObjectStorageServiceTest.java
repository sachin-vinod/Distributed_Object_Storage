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

    }
}
