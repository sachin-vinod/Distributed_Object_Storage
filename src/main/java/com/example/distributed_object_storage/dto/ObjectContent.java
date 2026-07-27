package com.example.distributed_object_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectContent {
    private String objectId;
    private byte[] data;
    private String contentType;
}
