package com.example.distributed_object_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectRequest {
    private String name;
    private byte[] content;
    private String contentType;
    private long size;
}
