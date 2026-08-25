package com.example.distributed_object_storage.storage;

public interface ObjectStore {

    void putObject(String storageKey, byte[] content, String contentType);

    byte[] getObject(String storageKey);

    boolean deleteObject(String storageKey);

    boolean exists(String storageKey);
}
