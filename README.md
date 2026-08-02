# Distributed_Object_Storage

A simple distributed object storage style service built with Spring Boot. It exposes REST endpoints for creating, retrieving, updating, and deleting objects.

## Architecture Overview

- `ObjectController` exposes the REST API.
- `ObjectStorageService` orchestrates object operations.
- `MetadataService` stores object metadata such as name, content type, size, timestamps, and status.
- `StorageService` writes object content to the local filesystem storage directory.

## Request Flow

1. Client uploads a file through `POST /api/objects`.
2. The controller builds an `ObjectRequest` from the uploaded multipart file.
3. The object service generates a SHA-256 object ID from the file name.
4. The storage service writes the file bytes to `storage/<objectId>`.
5. The metadata service records the object metadata.
6. Subsequent reads return metadata from the metadata service.

## Key Design Choices

- Multipart upload support via `@RequestParam("file") MultipartFile`.
- Metadata keeps a hashed object ID for lookup.
- Object content is stored under a simpler storage key based on the uploaded filename.
- Both are persisted under the `storage/` directory.
- Metadata is tracked separately from stored bytes to keep responsibilities clear.

## API Endpoints

- `POST /api/objects` - upload a file and create an object
- `GET /api/objects` - list all objects
- `GET /api/objects/{objectId}` - fetch metadata for an object
- `PUT /api/objects/{objectId}` - update an object
- `DELETE /api/objects/{objectId}` - delete an object

## Flow Diagram

```text
Client -> ObjectController -> ObjectStorageService
                              -> MetadataService
                              -> StorageService
```
