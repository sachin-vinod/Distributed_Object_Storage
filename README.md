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

## How to make GET and POST calls

POST (curl) - upload a file:

```bash
curl -v -X POST "http://localhost:8080/api/objects" \
  -F "file=@/path/to/file" \
  -H "Accept: application/json"
```

Typical successful response:

```http
HTTP/1.1 201 Created
Location: /api/objects/{objectId}
Content-Type: application/json

{ "objectId": "<objectId>", "name": "file.txt", "size": 1234 }
```

POST (JavaScript - fetch):

```js
const fd = new FormData();
fd.append('file', fileInput.files[0]);
fetch('http://localhost:8080/api/objects', { method: 'POST', body: fd })
  .then(r => r.json())
  .then(console.log);
```

GET list of objects (curl):

```bash
curl -s http://localhost:8080/api/objects
```

GET object metadata (curl):

```bash
curl -s http://localhost:8080/api/objects/{objectId}
```

## Request Flow (detailed)

1. Client sends `POST /api/objects` with a multipart file.
2. ObjectController validates the request and builds an ObjectRequest.
3. ObjectStorageService generates an objectId (SHA-256 of name/contents) and coordinates persistence:
   - StorageService writes the raw bytes to `storage/<objectId>`.
   - MetadataService persists metadata (name, contentType, size, timestamps).
4. Controller returns `201 Created` with `Location: /api/objects/{objectId}` and body containing object metadata.
5. Subsequent `GET /api/objects/{objectId}` returns stored metadata; `GET /api/objects` lists objects.

## Flow Diagram

```text
Client -> ObjectController -> ObjectStorageService
                              -> StorageService (writes storage/<objectId>)
                              -> MetadataService (persists metadata)
ObjectController <- 201 Created (Location: /api/objects/{objectId})
```
