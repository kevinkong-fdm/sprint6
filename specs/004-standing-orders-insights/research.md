# Research: Authenticated Standing Orders, Notifications, Statements, and Spending Insights

## Decision 1: Delivery scope and runtime boundary

- Decision: Scope implementation and validation to local-only execution.
- Rationale: The planning request explicitly requires local operation only; this keeps dependencies, environment assumptions, and validation deterministic for this increment.
- Alternatives considered: Multi-environment deployment (rejected as out of scope), cloud-first rollout (rejected by requirement).

## Decision 2: Platform timezone implementation

- Decision: Use fixed AEST semantics with `Australia/Brisbane` timezone handling for all schedule and statement month-boundary calculations.
- Rationale: `Australia/Brisbane` maps to UTC+10:00 without DST shifts, which preserves the clarified fixed AEST rule and avoids boundary ambiguity.
- Alternatives considered: UTC-only processing (rejected due to spec mismatch), per-customer timezone configuration (rejected in this version).

## Decision 3: Standing-order destination constraints

- Decision: Accept destination only as internal platform `destinationAccountId` and enforce same-customer ownership at create/update/execute validation points.
- Rationale: Directly matches clarified behavior and reduces cross-customer transfer risk.
- Alternatives considered: External bank destination fields (rejected by clarification), allowing same-platform cross-customer destination (rejected by authorization rule).

## Decision 4: Standing-order lifecycle and scheduling model

- Decision: Model standing-order state as `ACTIVE`, `PAUSED`, and `CANCELED` with deterministic transitions and next-due computation tied to configured frequency.
- Rationale: Supports required pause/resume/cancel behavior while keeping scheduler eligibility easy to reason about and test.
- Alternatives considered: Soft-delete-only lifecycle (insufficient for pause/resume), status-free cron records (weak auditability).

## Decision 5: Idempotency for setup and execution trigger

- Decision: Require idempotency key support for standing-order creation and manual execution-trigger API paths; duplicate keys return prior committed outcome.
- Rationale: Prevents duplicate standing orders or duplicate execution postings during retries and aligns with edge-case requirements.
- Alternatives considered: Client-only retry suppression (insufficient server guarantees), no idempotency (duplicate posting risk).

## Decision 6: Execution posting atomicity

- Decision: Persist each standing-order execution and related money movement updates in a single transaction; failure paths record deterministic non-posted outcomes.
- Rationale: Enforces no-partial-state guardrail and keeps audit trails consistent.
- Alternatives considered: Multi-step best-effort posting (partial state risk), asynchronous compensation workflow (unnecessary complexity for local scope).

## Decision 7: Notification behavior and preference policy

- Decision: Represent notification preferences as system-managed default-on state and reject preference modification requests with `NOTIFY-001`.
- Rationale: Satisfies fixed-notification policy while preserving explicit API behavior for unsupported preference updates.
- Alternatives considered: User-configurable channels in v1 (contradicts clarification), no preference resource at all (weak contract clarity for rejected updates).

## Decision 8: Monthly statement generation strategy

- Decision: Generate per-account, per-month statements from posted ledger activity, always returning a valid statement shape for closed months, including empty-activity months.
- Rationale: Meets statement completeness requirements and avoids null/partial statement payloads.
- Alternatives considered: Error on no-activity months (contradicts FR-017), precomputed monthly snapshots only (higher operational complexity for this scope).

## Decision 9: Spending-insight low-data behavior

- Decision: Return HTTP success with a deterministic `insufficientData` indicator and partial metrics when historical data is valid but inadequate for full comparison.
- Rationale: Matches clarification and enables UI to degrade gracefully without error handling branches.
- Alternatives considered: Error response on low data (explicitly rejected), suppressing comparison fields entirely (reduces deterministic contract behavior).

## Decision 10: Contract and validation gates

- Decision: Keep OpenAPI as the authoritative interface with deterministic error-code mappings and local contract/integration test gates before implementation tasks.
- Rationale: Prevents spec drift and ensures all clarified constraints are testable.
- Alternatives considered: Code-first API with post-hoc docs (high drift risk), unit-test-only verification (insufficient behavior coverage).
