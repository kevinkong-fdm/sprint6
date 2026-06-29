# Research: Authenticated Bank Account Operations

## Decision 1: Runtime environment scope

- Decision: Scope implementation and validation to local-only execution.
- Rationale: The user explicitly requested local-only operation for this planning cycle; this avoids unnecessary cloud deployment work and keeps testing deterministic.
- Alternatives considered: Production-ready multi-environment deployment (rejected as out of scope), staging-first rollout (rejected for this increment).

## Decision 2: API surface shape

- Decision: Use RESTful account and transaction endpoints: create/list/retrieve/update/delete accounts, account-scoped deposit/withdraw, cross-account transfer, and account transaction history.
- Rationale: Aligns with current repository API conventions and keeps each story independently testable through contract tests.
- Alternatives considered: RPC-style action endpoints only (less resource-centric), event-only asynchronous interface (does not satisfy immediate CRUD and balance movement stories).

## Decision 3: Monetary precision strategy

- Decision: Represent monetary amounts and balances using fixed-precision decimal values (`DECIMAL(19,4)` equivalent) across persistence and contract schemas.
- Rationale: Avoids floating-point rounding issues and ensures deterministic balance calculations.
- Alternatives considered: Binary floating point (rejected due precision risk), integer minor-units only (possible but increases conversion overhead for this stack).

## Decision 4: Delete-with-balance settlement behavior

- Decision: For account deletion requests where available balance is non-zero, require a closeout destination account and execute an atomic closeout transfer before deleting the source account.
- Rationale: Preserves funds while honoring the clarified policy that deletion can proceed with any available balance when no pending movement activity exists.
- Alternatives considered: Reject non-zero balance deletes (conflicts with clarified policy), write-off residual funds (financial integrity risk), asynchronous payout workflow (too complex for local-only scope).

## Decision 5: Pending movement eligibility rule

- Decision: Model movement lifecycle states and block account deletion when any movement for the target account is in `PENDING` status.
- Rationale: Matches deletion guardrail requirements and yields deterministic deletion eligibility outcomes.
- Alternatives considered: Best-effort deletion with delayed cleanup (non-deterministic), ignoring pending state (integrity risk).

## Decision 6: Transfer atomicity pattern

- Decision: Persist transfer as a paired debit/credit operation linked by a transfer identifier in a single database transaction.
- Rationale: Guarantees all-or-nothing balance updates and supports auditable transfer history.
- Alternatives considered: Two independent operations without linkage (partial failure risk), eventual-consistency saga pattern (unnecessary for local single-service scope).

## Decision 7: Idempotency handling for movement endpoints

- Decision: Support idempotency keys for deposit, withdrawal, transfer, and delete-closeout requests.
- Rationale: Prevents duplicate posting from client retries and directly addresses edge-case requirements.
- Alternatives considered: No idempotency (duplicate movement risk), client-only retry suppression (insufficient server-side protection).

## Decision 8: Authorization model

- Decision: Authorize operations by account ownership derived from authenticated customer identity in JWT/session context.
- Rationale: Satisfies story requirements that operations are available only after authentication and must block cross-account access.
- Alternatives considered: Role-only authorization without ownership checks (insufficient protection), anonymous access (out of scope and insecure).

## Decision 9: Contract and error envelope strategy

- Decision: Use a standardized error envelope (`errorCode`, `message`, `correlationId`) for all failures and map each failure path to spec-defined codes.
- Rationale: Supports deterministic client handling and operational traceability.
- Alternatives considered: Endpoint-specific ad-hoc errors (contract drift risk), plain-text errors (insufficient machine-readability).

## Decision 10: Testing strategy for local scope

- Decision: Treat backend unit/integration tests and contract tests as mandatory local gates; frontend tests run when UI wiring is implemented.
- Rationale: Keeps validation focused on high-risk financial behavior while preserving rapid local feedback.
- Alternatives considered: Unit-only testing (insufficient for API contract confidence), full end-to-end environment matrix (out of local-only scope).
