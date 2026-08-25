package com.example.distributed_object_storage.controller;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectResponse;
import com.example.distributed_object_storage.service.ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/objects")
public class ObjectController {

    private static final Logger log = LoggerFactory.getLogger(ObjectController.class);

    private final ObjectStorageService objectStorageService;

    public ObjectController(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ObjectResponse> createObject(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Incoming POST /api/objects/{} -> file='{}' ({} bytes, type='{}')", 
                userId, file.getOriginalFilename(), file.getSize(), file.getContentType());

        ObjectRequest request = new ObjectRequest();
        request.setName(file.getOriginalFilename());
        request.setContent(file.getBytes());
        request.setContentType(file.getContentType());
        request.setSize(file.getSize());

        ObjectResponse response = objectStorageService.createObject(userId, request);
        log.info("Returning HTTP 201 CREATED for file='{}' (user='{}')", file.getOriginalFilename(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}/{filename}")
    public ResponseEntity<byte[]> getObject(
            @PathVariable String userId,
            @PathVariable String filename) {
        log.info("Incoming GET /api/objects/{}/{}", userId, filename);

        String storageKey = userId + "/" + filename;
        byte[] content = objectStorageService.getObjectContent(storageKey);
        if (content == null) {
            log.warn("Object not found for storageKey='{}' -> returning 404", storageKey);
            return ResponseEntity.notFound().build();
        }
        log.info("Returning HTTP 200 OK for storageKey='{}' ({} bytes)", storageKey, content.length);
        return ResponseEntity.ok(content);
    }

    @DeleteMapping("/{userId}/{filename}")
    public ResponseEntity<Void> deleteObject(
            @PathVariable String userId,
            @PathVariable String filename) {
        log.info("Incoming DELETE /api/objects/{}/{}", userId, filename);

        boolean deleted = objectStorageService.deleteObject(userId, filename);
        if (!deleted) {
            log.warn("Failed to delete object: userId='{}', filename='{}' -> returning 404", userId, filename);
            return ResponseEntity.notFound().build();
        }
        log.info("Returning HTTP 204 NO_CONTENT for userId='{}', filename='{}'", userId, filename);
        return ResponseEntity.noContent().build();
    }
}
