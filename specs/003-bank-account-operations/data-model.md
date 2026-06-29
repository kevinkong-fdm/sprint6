# Data Model: Authenticated Bank Account Operations

All entities below are persisted in MySQL for local-only execution.

## Entity: BankAccount

- Purpose: Customer-owned account for checking/savings operations and balance management.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| account_id | UUID | Primary key, immutable | System-generated account identifier |
| customer_id | UUID | Required, indexed | Authenticated account owner identity |
| account_type | ENUM(CHECKING, SAVINGS) | Required, immutable | Allowed account categories |
| nickname | String | Optional, max 80 | Mutable display metadata |
| currency_code | CHAR(3) | Required, immutable | ISO currency code (v1 default single currency) |
| available_balance | DECIMAL(19,4) | Required, default 0.0000 | Spendable balance |
| ledger_balance | DECIMAL(19,4) | Required, default 0.0000 | Book balance for reconciliation |
| status | ENUM(ACTIVE, PENDING_DELETE) | Required | `PENDING_DELETE` is transient during closeout+delete transaction |
| created_at | TIMESTAMP | Required, immutable | Creation audit field |
| updated_at | TIMESTAMP | Required | Last mutation audit field |

Validation rules:
- `account_type` must be CHECKING or SAVINGS.
- `available_balance` and `ledger_balance` use fixed precision and cannot be null.
- Immutable fields cannot be changed by update operations.

## Entity: AccountMovement

- Purpose: Immutable transaction ledger events for deposit, withdrawal, transfer, and closeout settlement.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| movement_id | UUID | Primary key | Movement identifier |
| account_id | UUID | Required, FK -> BankAccount.account_id (historical retention policy applies) | Target account |
| movement_type | ENUM(DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT, CLOSEOUT_DEBIT, CLOSEOUT_CREDIT) | Required | Financial event category |
| amount | DECIMAL(19,4) | Required, > 0 | Positive amount for all movement types |
| direction | ENUM(CREDIT, DEBIT) | Required | Balance direction for this account |
| status | ENUM(PENDING, POSTED, FAILED) | Required | Used for pending-eligibility checks |
| balance_before | DECIMAL(19,4) | Required | Snapshot before posting |
| balance_after | DECIMAL(19,4) | Required | Snapshot after posting |
| idempotency_key | String | Optional, indexed | Retry safety token from client |
| correlation_id | String | Required | Traceability identifier |
| reference_id | UUID | Optional | Links paired movements (transfer/closeout) |
| created_at | TIMESTAMP | Required | Event creation timestamp |
| posted_at | TIMESTAMP | Nullable | Set when status becomes POSTED |

Validation rules:
- `amount` must be greater than zero.
- For withdrawals/transfers, posting is rejected if resulting `available_balance` would be negative.
- Duplicate idempotency keys for the same account and operation return the original posted result.

## Entity: AccountDeleteRequestAudit

- Purpose: Auditable record of account deletion eligibility checks and outcomes.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| delete_audit_id | UUID | Primary key | Audit identifier |
| account_id | UUID | Required | Account requested for deletion |
| actor_customer_id | UUID | Required | Authenticated caller |
| requested_closeout_destination_account_id | UUID | Nullable | Required by rule when balance is non-zero |
| eligibility_result | ENUM(ALLOWED, REJECTED_PENDING_MOVEMENT, REJECTED_INVALID_DESTINATION, REJECTED_UNAUTHORIZED) | Required | Deterministic eligibility outcome |
| error_code | String | Nullable | Set when rejected |
| correlation_id | String | Required | Traceability identifier |
| created_at | TIMESTAMP | Required | Audit timestamp |

Validation rules:
- If source account available balance is non-zero, destination account must be present and authorized.
- If any `PENDING` movement exists for source account, deletion is rejected.

## Relationships

- BankAccount 1..* AccountMovement
- BankAccount 1..* AccountDeleteRequestAudit
- Transfer/closeout pairings use `AccountMovement.reference_id` to link debit/credit events.

## State Transitions

### BankAccount

- `ACTIVE -> PENDING_DELETE -> DELETED`
- `PENDING_DELETE` exists only inside the delete transaction boundary.
- After `DELETED`, account retrieval/list/update/movement operations return account-not-found or authorization errors.

### AccountMovement

- `PENDING -> POSTED`
- `PENDING -> FAILED`
- Only `POSTED` movements mutate account balances.

## Invariants

- Account balances are updated only by posted movement events.
- Transfer and closeout debit/credit pairs commit atomically.
- Deletion eligibility requires no pending source-account movement.
- Error responses map to defined feature error codes and include correlation id.
