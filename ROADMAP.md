# Distributed Object Storage — Checkpoints & Roadmap

---

## Checkpoint 1 — Clean storage abstraction

### Goal
Separate your application logic from MinIO.

#### Current:
```
ObjectStorageService
        |
        v
      MinIO
```

#### Target:
```
ObjectStorageService
        |
        v
    ObjectStore
        |
        v
MinioObjectStore
```

#### Potential interface:
```java
interface ObjectStore {
    putObject(...)
    getObject(...)
    deleteObject(...)
    exists(...)
}
```

### Acceptance criteria
- [x] `ObjectStorageService` does not directly depend on MinIO implementation details.
- [x] MinIO implementation is behind an `ObjectStore` interface.
- [x] Existing upload/download/delete functionality continues working.
- [x] You can replace MinIO with another implementation without changing business logic.

### Interview concept
Dependency inversion / pluggable storage backend.

---

## Checkpoint 2 — Proper object metadata

Create a clear metadata model.

### ObjectMetadata
```
objectId
objectName
size
contentType
createdAt
version
storageKey
```

Don't mix this with the actual bytes.

```
Metadata Store
       |
       v
Object information

MinIO
       |
       v
Actual bytes
```

### Acceptance criteria
- [ ] Every uploaded object has a unique object ID.
- [ ] Metadata can be retrieved independently.
- [ ] Metadata contains enough information to locate the object.
- [ ] Deleting an object also handles its metadata correctly.
- [ ] Object bytes are not stored inside your metadata database.

### Interview concept
Metadata/data separation.

---

## Checkpoint 3 — Content-based object identification

Use SHA-256 consistently.

```
file
 |
 v
SHA-256
 |
 v
objectId
```

For example:
```
abc.pdf
    |
    v
SHA256(...)
    |
    v
91a82f...
```

### Acceptance criteria
- [ ] Object ID generation is deterministic.
- [ ] Same content produces the same hash.
- [ ] Different content produces different IDs with extremely high probability.
- [ ] Object ID is independent of the original filename.

### Decide now
You should explicitly decide whether this is:
- **content-addressable storage**
or simply:
- **unique object IDs generated using SHA-256.**

Don't accidentally claim deduplication unless you actually implement it.

### Interview concept
Hashing / content addressing / deduplication trade-offs.

---

## Checkpoint 4 — Introduce storage nodes

This is where the project starts becoming genuinely distributed.

Instead of:
```
Application
    |
    v
MinIO
```

create the abstraction:
```
StorageCluster
      |
 +----+----+
 |         |
 v         v
Node 1    Node 2
MinIO     MinIO
```

Each node should have:
```
nodeId
endpoint
status
capacity
```

For example:
```
Node-1 -> localhost:9001
Node-2 -> localhost:9002
Node-3 -> localhost:9003
```

You can run multiple MinIO instances/containers.

### Acceptance criteria
- [ ] At least 2 storage nodes can run simultaneously.
- [ ] Application can identify each node independently.
- [ ] Object can be uploaded to a selected node.
- [ ] Application doesn't hard-code a single MinIO endpoint.
- [ ] Node configuration is externalized.

### Interview concept
Horizontal scaling / service discovery / node abstraction.

---

## Checkpoint 5 — Partitioning / object placement

Now determine:  
Given an object, which node should store it?

Start simple:
```
hash(objectId) % numberOfNodes
```

Later you can replace this with consistent hashing.

```
Object ID
   |
   v
Hash
   |
   v
Partition
   |
   v
Storage Node
```

### Acceptance criteria
- [ ] Same object consistently maps to the same partition.
- [ ] Placement is deterministic.
- [ ] Application can calculate the responsible node.
- [ ] Placement logic is isolated from upload/download logic.
- [ ] Adding/removing a node does not require changing application code.

### Interview concept
Partitioning / consistent hashing / data distribution.

---

## Checkpoint 6 — Placement metadata

Now explicitly store:

### ObjectPlacement
```
objectId
partitionId
primaryNode
replicaNodes
version
```

### Example:
```
Object A

Partition: P42
Primary: Node-1
Replicas:
    Node-2
    Node-3
Version: 5
```

### Acceptance criteria
- [ ] System can determine the primary node for any object.
- [ ] System can determine all replica nodes.
- [ ] Placement survives application restart.
- [ ] Placement can be updated independently of object metadata.
- [ ] Placement version is tracked.

### Interview concept
Control plane vs data plane.

This is an important concept:
```
Metadata / placement
       =
Control plane

Actual object bytes
       =
Data plane
```

---

## Checkpoint 7 — Replication

Now introduce replication factor.

For example:  
**Replication Factor = 3**

Object:
```
             Object A
                 |
        +--------+--------+
        |        |        |
        v        v        v
      Node 1   Node 2   Node 3
     Primary   Replica  Replica
```

### Acceptance criteria
- [ ] Every object has configurable replication factor.
- [ ] Object is written to primary and replicas.
- [ ] Replica placement is deterministic.
- [ ] Replica nodes are different from the primary.
- [ ] Placement metadata records all replicas.
- [ ] A replica failure doesn't make the object immediately unavailable.

### Interview concept
Replication factor / durability / availability.

---

## Checkpoint 8 — Write acknowledgement / quorum

Now decide:  
When do we tell the client the upload succeeded?

For RF=3, you could choose:
- `3/3 -> success`
or:
- `2/3 -> success`

I would initially implement configurable quorum.

### Example:
```
Replication Factor = 3
Write Quorum = 2
Node 1 ✓
Node 2 ✓
Node 3 X

2/3
 |
 v
SUCCESS
```

### Acceptance criteria
- [ ] Write quorum is configurable.
- [ ] Client receives success only after quorum is satisfied.
- [ ] Failed replicas are tracked.
- [ ] Failed replication can be retried/repaired.
- [ ] System does not claim full replication when only quorum succeeded.

### Interview concept
Quorum / consistency vs availability.

---

## Checkpoint 9 — Failure detection and read failover

Now implement your idea about secondary nodes.

### Read:
```
Client
  |
  v
Primary
  |
  X
  |
Replica 1
  |
  X
  |
Replica 2
  |
  v
Success
```

But don't simply retry the same node three times.

Have something like:
```
attempt 1 -> primary
attempt 2 -> replica 1
attempt 3 -> replica 2
```

Also distinguish:
```
timeout
connection refused
503
```
from:
```
404
```

### Acceptance criteria
- [ ] Primary node is tried first.
- [ ] Transient node failures trigger failover.
- [ ] Replica nodes can serve reads.
- [ ] Retry count is configurable.
- [ ] Same failed node isn't blindly retried repeatedly.
- [ ] 404/object-not-found is not automatically treated as node failure.
- [ ] Node health/status is tracked.

### Interview concept
Failure detection / retry / failover.

---

## Checkpoint 10 — Replica recovery

This is essential.

Suppose:
```
Node 1  ← primary
Node 2  ← replica
Node 3  ← replica
```

Node 3 dies.

You now have:
```
Node 1
Node 2
```

Only two copies exist.

When Node 4 becomes available:
```
Node 1
Node 2
Node 4
```

The system needs to restore:  
**Replication Factor = 3**

### Acceptance criteria
- [ ] System detects missing replicas.
- [ ] Object is copied to a healthy replacement node.
- [ ] Placement metadata is updated only after successful replication.
- [ ] Failed replication can be retried.
- [ ] System eventually restores the configured replication factor.

### Interview concept
Self-healing storage.

---

## Checkpoint 11 — Rebalancing

This is one of the most important checkpoints.

Suppose:
```
Node 1
Node 2
Node 3
```

Then Node 4 joins.

Don't move everything.

Use partitions:
```
P1 -> Node 1
P2 -> Node 1
P3 -> Node 2
P4 -> Node 2
P5 -> Node 3
P6 -> Node 3
```

After Node 4 joins:
```
P1 -> Node 1
P2 -> Node 4   <- moved
P3 -> Node 2
P4 -> Node 2
P5 -> Node 3
P6 -> Node 4   <- moved
```

Only affected partitions move.

### Acceptance criteria
- [ ] Adding a node triggers rebalancing.
- [ ] System identifies partitions whose ownership changes.
- [ ] Only affected objects are migrated.
- [ ] Object data is copied before metadata ownership changes.
- [ ] Failed migrations can be retried.
- [ ] Rebalancing doesn't make existing objects unavailable.
- [ ] Removing a node also triggers redistribution.
- [ ] Replication requirements are maintained after rebalancing.

### Interview concept
Consistent hashing / data movement minimization / cluster membership changes.

---

## Checkpoint 12 — Observability and testing

Before calling the project interview-ready, add visibility.

You should be able to see:
- Object upload
- Object placement
- Replication
- Node failure
- Failover
- Rebalancing
- Recovery

### Useful metrics:
- upload latency
- download latency
- replication success/failure
- rebalance progress
- objects per node
- storage used per node
- failed requests
- node health

### Acceptance criteria
- [ ] Logs identify object ID and relevant storage node.
- [ ] Replication failures are visible.
- [ ] Rebalancing progress is visible.
- [ ] Node failures are detectable.
- [ ] Integration tests cover node failure.
- [ ] Integration tests cover adding a node.
- [ ] Integration tests cover removing a node.
- [ ] Integration tests verify replication recovery.

---

## Final project state

Once all of this is implemented, your system becomes:

```
                         Client
                           |
                           v
                    Load Balancer
                           |
                    Spring Boot API
                           |
                           v
                    Object Manager
                           |
             +-------------+-------------+
             |                           |
             v                           v
       Metadata Store              Placement Manager
                                         |
                                         v
                                  Partition Manager
                                         |
                    +--------------------+--------------------+
                    |                    |                    |
                    v                    v                    v
                 Node 1               Node 2               Node 3
                 MinIO                MinIO                MinIO
                    |                    |                    |
                    +---------- Replication ------------------+
                                         |
                              +----------+----------+
                              |                     |
                              v                     v
                       Failure Detector        Rebalancer
                              |                     |
                              v                     v
                       Replica Recovery       Data Migration
```

And your core data model becomes approximately:

```
Object
├── objectId
├── metadata
└── version

Placement
├── objectId
├── partitionId
├── primaryNode
├── replicaNodes[]
└── version

StorageNode
├── nodeId
├── endpoint
├── status
├── capacity
└── usedCapacity
```

---

## Recommended implementation order

Don't implement this in the order of "coolest feature." Use dependency order:

1. Storage abstraction
       ↓
2. Metadata model
       ↓
3. Object IDs / hashing
       ↓
4. Multiple MinIO nodes
       ↓
5. Partitioning
       ↓
6. Placement metadata
       ↓
7. Replication
       ↓
8. Write quorum
       ↓
9. Failure detection
       ↓
10. Read failover
       ↓
11. Replica recovery
       ↓
12. Rebalancing
       ↓
13. Observability + failure testing

---

## One rule for your GitHub/recruiter story

Create a Git checkpoint/tag for each major stage, for example:

- `v1.0`  basic-object-storage
- `v1.1`  minio-backend
- `v1.2`  storage-node-abstraction
- `v1.3`  partitioning
- `v1.4`  replication
- `v1.5`  failover
- `v1.6`  replica-recovery
- `v1.7`  rebalancing
- `v2.0`  distributed-object-storage
