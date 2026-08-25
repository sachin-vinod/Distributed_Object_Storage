package com.example.distributed_object_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectResponse {
    private String filename;
    private String contentType;
    private long size;
}
