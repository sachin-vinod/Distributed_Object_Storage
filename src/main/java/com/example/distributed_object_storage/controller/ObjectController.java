package com.example.distributed_object_storage.controller;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectMetadata;
import com.example.distributed_object_storage.service.ObjectStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/objects")
public class ObjectController {

    private final ObjectStorageService objectStorageService;

    public ObjectController(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ObjectMetadata> createObject(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        ObjectRequest request = new ObjectRequest();
        request.setName(file.getOriginalFilename());
        request.setContent(file.getBytes());
        request.setContentType(file.getContentType());
        request.setSize(file.getSize());

        ObjectMetadata metadata = objectStorageService.createObject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(metadata);
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<ObjectMetadata> getObject(@PathVariable String objectId) {
        ObjectMetadata metadata = objectStorageService.getObject(objectId);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }

    @GetMapping
    public ResponseEntity<List<ObjectMetadata>> getAllObjects() {
        List<ObjectMetadata> objects = objectStorageService.getAllObjects();
        return ResponseEntity.ok(objects);
    }

    @PutMapping("/{objectId}")
    public ResponseEntity<ObjectMetadata> updateObject(
            @PathVariable String objectId,
            @RequestBody ObjectRequest request) {
        ObjectMetadata metadata = objectStorageService.updateObject(objectId, request);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }

    @DeleteMapping("/{objectId}")
    public ResponseEntity<Void> deleteObject(@PathVariable String objectId) {
        boolean deleted = objectStorageService.deleteObject(objectId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
