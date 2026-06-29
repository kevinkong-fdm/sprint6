# Quickstart: Authenticated Bank Account Operations

This feature is scoped for local execution only.

Use this guide to validate behavior end-to-end against:
- Contract: [contracts/account.openapi.yaml](./contracts/account.openapi.yaml)
- Data model: [data-model.md](./data-model.md)
- Feature specification: [spec.md](./spec.md)

## Endpoint Map

- `POST /accounts` -> Create Account (Checking/Savings)
- `GET /accounts/{accountId}` -> Retrieve Account Details
- `GET /accounts` -> List Customer Accounts
- `PATCH /accounts/{accountId}` -> Update Account
- `DELETE /accounts/{accountId}` -> Delete Account (eligibility + optional closeout destination)
- `POST /accounts/{accountId}/deposits` -> Deposit
- `POST /accounts/{accountId}/withdrawals` -> Withdraw
- `POST /transfers` -> Transfer Funds
- `GET /accounts/{accountId}/transactions` -> Get Transaction History

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+ (or 22 LTS)
- MySQL 8.x (local install or Docker)

## Local Setup

1. Start MySQL locally.

Docker option:

```bash
docker compose up -d mysql
```

2. Ensure database exists.

```sql
CREATE DATABASE IF NOT EXISTS banking;
```

3. Start backend.

```bash
cd backend
..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd flyway:migrate
..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd spring-boot:run
```

Expected outcome:
- Backend starts on `http://localhost:8080` with account operation endpoints available.

4. Start frontend.

```bash
cd ../frontend
npm install
npm run dev
```

Expected outcome:
- Frontend starts on local Vite URL and can authenticate against backend.

## Validation Scenarios

### Scenario 1: Create checking and savings accounts

1. Authenticate and obtain bearer token.
2. Call `POST /accounts` twice (CHECKING then SAVINGS).

Expected:
- HTTP 201 for both
- `availableBalance` and `ledgerBalance` are `0.0000`
- Unique `accountId` returned per create request

### Scenario 2: Retrieve and list accounts

1. Call `GET /accounts/{accountId}` for one created account.
2. Call `GET /accounts` for current authenticated customer.

Expected:
- Retrieve returns HTTP 200 with complete account details
- List returns HTTP 200 with both accounts

### Scenario 3: Update account metadata

1. Call `PATCH /accounts/{accountId}` with valid mutable field (e.g., nickname).
2. Attempt immutable-field update via unsupported payload.

Expected:
- Valid update returns HTTP 200 with updated value
- Immutable-field attempt returns contract-mapped error (`ACCT-UPD-002`)

### Scenario 4: Deposit and withdraw

1. Post deposit via `POST /accounts/{accountId}/deposits`.
2. Post valid withdrawal via `POST /accounts/{accountId}/withdrawals`.
3. Post excessive withdrawal amount.

Expected:
- Deposit/valid withdrawal return HTTP 201
- Insufficient withdrawal returns conflict with `TXN-WDR-002`
- Balances and transaction history reflect posted movements only

### Scenario 5: Transfer funds

1. Call `POST /transfers` from source to destination account with sufficient funds.
2. Retry same transfer request with same idempotency key.
3. Attempt transfer with insufficient source funds.

Expected:
- Valid transfer returns HTTP 201 with paired movement records
- Idempotent retry does not duplicate posted movement
- Insufficient transfer returns conflict with `TXN-TRF-003`

### Scenario 6: Delete account eligibility and closeout

1. Ensure source account has non-zero balance and no pending movement.
2. Call `DELETE /accounts/{accountId}` with `closeoutDestinationAccountId` in request body.
3. Attempt delete while pending movement exists.

Expected:
- Eligible delete returns HTTP 204
- Source account no longer retrievable/listed
- Pending-movement delete returns eligibility error (`ACCT-DEL-002`)

### Scenario 7: Transaction history

1. Call `GET /accounts/{accountId}/transactions` after create/deposit/withdraw/transfer activity.
2. Call with invalid date filter range.

Expected:
- HTTP 200 with descending event order and stable pagination
- Invalid filter returns `TXN-HIS-001`

### Scenario 8: Authentication and authorization guardrails

1. Repeat account operation without token.
2. Attempt operation on account not owned by authenticated customer.

Expected:
- Unauthenticated call returns `AUTH-ACC-001`
- Unauthorized ownership call returns `AUTH-ACC-002`

## Suggested Local Test Commands

## Local Validation Command Matrix

| Story Coverage | Command | Expected Outcome |
|---|---|---|
| Backend compile baseline | `cd backend && ..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd compile` | Compile succeeds with account module wired |
| Account contract tests (US1-US5) | `cd backend && ..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd -Pcontract -Dtest=com.example.banking.account.contract.*Test test` | Contract tests pass and endpoint error mappings remain deterministic |
| Backend broader verification | `cd backend && ..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd test` | Feature integration paths remain green |
| Frontend behavior checks | `cd frontend && npm run test` | UI/API integration tests pass |
| Frontend production build | `cd frontend && npm run build` | Type-checking and bundle build succeed |

Backend test suite:

```bash
cd backend
..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd test
```

Backend contract profile:

```bash
cd backend
..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd verify -Pcontract
```

Frontend checks:

```bash
cd frontend
npm run test
npm run build
```

Expected outcome:
- Contract and integration checks pass for in-scope operations
- Frontend build/test pass when UI wiring for these operations is included
