# Research: Customer Profile Lifecycle Management

## Decision 1: Identity uniqueness key

- Decision: Use normalized email as the unique customer identity attribute in v1 customer profile operations.
- Rationale: The current platform already relies on email-centric identity patterns; normalization prevents case-variant duplicates and keeps create/get/update consistency predictable.
- Alternatives considered: Composite identity keys (higher complexity for v1), phone-number primary identity (higher validation/ownership ambiguity).

## Decision 2: Authorization scope model

- Decision: Allow any authenticated operator to perform full customer CRUD in v1.
- Rationale: This follows explicit clarification and avoids role-segmentation complexity during initial lifecycle rollout.
- Alternatives considered: Role-split admin/support model (rejected by clarification), read-only support role (rejected by clarification).

## Decision 3: Update concurrency behavior

- Decision: Apply deterministic last-write-wins semantics for concurrent valid profile updates.
- Rationale: This directly matches clarified policy and simplifies API interaction patterns for clients while retaining deterministic outcomes.
- Alternatives considered: Optimistic concurrency token/ETag conflict rejection (rejected by clarification), lock-based update serialization (higher operational complexity).

## Decision 4: Delete strategy

- Decision: Implement hard delete with physical customer record removal.
- Rationale: Explicitly required by clarification for v1 behavior.
- Alternatives considered: Soft delete/deactivation (rejected by clarification), delete with restore grace period (rejected by clarification).

## Decision 5: Dependency handling for deletes

- Decision: Cascade delete dependent records within the same transactional delete operation.
- Rationale: Clarified policy requires automatic cascading removal to avoid orphaned domain data.
- Alternatives considered: Reject delete on dependency presence (rejected by clarification), detach dependent records (inconsistent lifecycle integrity).

## Decision 6: API contract shape

- Decision: Provide RESTful customer lifecycle endpoints: create customer, get by identifier, patch customer, and delete customer.
- Rationale: Aligns with existing backend API style and supports straightforward contract testing.
- Alternatives considered: RPC-style endpoint naming (less resource-centric), event-only interface (insufficient for direct CRUD workflows).

## Decision 7: Error envelope and traceability

- Decision: Keep a standardized error response containing error code, user-safe message, and correlation identifier.
- Rationale: Required by spec and essential for operational debugging and support traceability.
- Alternatives considered: Endpoint-specific ad-hoc error payloads (contract drift risk), plain-text error responses (insufficient machine readability).

## Decision 8: Persistence and migration approach

- Decision: Persist customer lifecycle entities in MySQL with Flyway-managed schema migrations.
- Rationale: Reuses existing repository infrastructure and keeps schema evolution auditable.
- Alternatives considered: Separate datastore for customer profile domain (unnecessary operational split for v1), manual schema management (higher drift risk).

## Decision 9: Testing strategy

- Decision: Treat contract tests and backend integration tests as mandatory gates; include frontend tests only if customer UI integration is implemented.
- Rationale: Ensures spec-first and contract-first correctness while keeping backend-first scope focused.
- Alternatives considered: Unit-tests-only strategy (insufficient for contract confidence), full frontend mandate regardless of UI scope (unnecessary initial overhead).

## Decision 10: Hard delete safety guardrail

- Decision: Require hard delete + cascade behavior to execute atomically and rollback fully on cascade failure.
- Rationale: Prevents partially deleted customer domains and supports deterministic lifecycle behavior.
- Alternatives considered: Best-effort cascading with partial completion (data integrity risk), asynchronous cleanup jobs only (temporal inconsistency risk).

## Decision 11: Execution environment

- Decision: Scope this feature to local-only execution on a developer workstation.
- Rationale: This increment is intended for local validation and does not require cloud deployment hardening or high-availability infrastructure.
- Alternatives considered: Production-ready environment assumptions (rejected for current scope), distributed multi-node deployment in v1 planning (unnecessary complexity).
