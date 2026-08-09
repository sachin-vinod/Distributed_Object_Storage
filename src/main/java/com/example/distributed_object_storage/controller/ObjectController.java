package com.example.distributed_object_storage.controller;

import com.example.distributed_object_storage.dto.ObjectRequest;
import com.example.distributed_object_storage.dto.ObjectResponse;
import com.example.distributed_object_storage.service.ObjectStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/objects")
public class ObjectController {

    private final ObjectStorageService objectStorageService;

    public ObjectController(ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ObjectResponse> createObject(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        ObjectRequest request = new ObjectRequest();
        request.setName(file.getOriginalFilename());
        request.setContent(file.getBytes());
        request.setContentType(file.getContentType());
        request.setSize(file.getSize());

        ObjectResponse response = objectStorageService.createObject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}/{filename}")
    public ResponseEntity<byte[]> getObject(
            @PathVariable String userId,
            @PathVariable String filename) {
        String storageKey = userId + "/" + filename;
        byte[] content = objectStorageService.getObjectContent(storageKey);
        if (content == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(content);
    }

    @DeleteMapping("/{userId}/{filename}")
    public ResponseEntity<Void> deleteObject(
            @PathVariable String userId,
            @PathVariable String filename) {
        boolean deleted = objectStorageService.deleteObject(userId, filename);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
