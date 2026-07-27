package com.example.distributed_object_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata {
    private String id;
    private String name;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
