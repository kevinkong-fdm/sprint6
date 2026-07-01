# Quickstart: Authenticated Standing Orders, Statements, and Spending Insights

This feature is scoped for local execution only.

Use this guide to validate behavior end-to-end against:
- Contract: [contracts/standing-orders-insights.openapi.yaml](./contracts/standing-orders-insights.openapi.yaml)
- Data model: [data-model.md](./data-model.md)
- Feature specification: [spec.md](./spec.md)

## Endpoint Map

- `POST /standing-orders` -> Create standing order
- `GET /standing-orders` -> List standing orders
- `GET /standing-orders/{standingOrderId}` -> Retrieve standing order
- `PATCH /standing-orders/{standingOrderId}` -> Update mutable standing-order fields
- `POST /standing-orders/{standingOrderId}/pause` -> Pause standing order
- `POST /standing-orders/{standingOrderId}/resume` -> Resume standing order
- `POST /standing-orders/{standingOrderId}/cancel` -> Cancel standing order
- `POST /standing-orders/{standingOrderId}/executions/trigger` -> Trigger execution attempt
- `GET /standing-orders/{standingOrderId}/executions` -> List execution history
- `POST /statements/monthly/generate` -> Generate monthly statement
- `GET /statements/monthly` -> Retrieve monthly statement
- `GET /insights/spending` -> Retrieve spending insights

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+ (or 22 LTS)
- MySQL 8.x

## Local Setup

1. Start MySQL locally.

Docker option:

```bash
docker compose up -d mysql
```

2. Ensure the backend database exists.

```sql
CREATE DATABASE IF NOT EXISTS banking_auth;
```

3. Start backend.

```bash
cd backend
..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd flyway:migrate
..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd spring-boot:run
```

Expected outcome:
- Backend starts on `http://localhost:8080` with standing-order, statement, and insight endpoints available.

4. Start frontend.

```bash
cd ../frontend
npm install
npm run dev
```

Expected outcome:
- Frontend starts on local Vite URL and can authenticate against backend locally.

## Validation Scenarios

### Scenario 1: Create standing order with internal same-customer destination

1. Authenticate and obtain bearer token.
2. Call `POST /standing-orders` with valid source account, destination account owned by same authenticated customer, schedule, and amount.

Expected:
- HTTP 201 with standing-order ID and next execution timestamp
- `status` is `ACTIVE`
- Destination ownership validation passes

### Scenario 2: Reject invalid standing-order destination rules

1. Submit setup payload with external bank destination fields.
2. Submit setup payload with destination account owned by another customer.

Expected:
- External destination rejected with `SO-SET-004`
- Cross-customer destination rejected with `SO-SET-005`

### Scenario 3: Update, pause, resume, and cancel lifecycle

1. Call `PATCH /standing-orders/{standingOrderId}` with mutable fields.
2. Call `POST /standing-orders/{standingOrderId}/pause`.
3. Call `POST /standing-orders/{standingOrderId}/resume`.
4. Call `POST /standing-orders/{standingOrderId}/cancel`.

Expected:
- Update returns HTTP 200 when valid
- Immutable field update returns `SO-UPD-002`
- Status transitions are deterministic and `CANCELED` is terminal

### Scenario 4: Trigger execution and verify deterministic outcomes

1. Call `POST /standing-orders/{standingOrderId}/executions/trigger` with idempotency key.
2. Retry same trigger with same idempotency key.
3. Trigger an execution with insufficient source funds.

Expected:
- First trigger returns HTTP 201 with execution record
- Retry does not duplicate posting and returns prior result
- Insufficient funds path returns `SO-EXE-001` and no partial posted state

### Scenario 5: Generate and retrieve monthly statements

1. Call `POST /statements/monthly/generate` for a valid closed month and owned account.
2. Call `GET /statements/monthly` for the same month.
3. Generate statement for valid month with no activity.
4. Request a future month.

Expected:
- Valid statement includes opening/closing balances and totals
- Retrieval returns deterministic statement payload
- No-activity month returns success with zeroed totals
- Invalid/future month request returns `STMT-001`

### Scenario 6: Spending insights with sufficient and insufficient history

1. Call `GET /insights/spending` for valid period with historical data.
2. Call same endpoint for valid period with insufficient history.
3. Call endpoint with invalid comparison window.

Expected:
- Valid data returns categorized totals and period-over-period deltas
- Low-data case returns HTTP 200 with `insufficientData = true` and partial metrics
- Invalid filters return `INS-001`

### Scenario 7: Authentication and ownership guardrails

1. Call representative endpoints without authentication token.
2. Call representative endpoints for resources owned by another customer.

Expected:
- Unauthenticated calls return `AUTH-FEAT-001`
- Unauthorized ownership calls return `AUTH-FEAT-002`

## Local Validation Command Matrix

| Story Coverage | Command | Expected Outcome |
|---|---|---|
| Backend compile baseline | `cd backend && ..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd compile` | Compile succeeds with feature modules wired |
| Backend tests | `cd backend && ..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd test` | Feature integration and unit tests pass |
| Contract profile tests | `cd backend && ..\\.tools\\apache-maven-3.9.9\\bin\\mvn.cmd verify -Pcontract` | Contract mappings and error codes remain deterministic |
| Frontend tests | `cd frontend && npm run test` | UI/API behavior tests pass |
| Frontend build | `cd frontend && npm run build` | Type checking and production build succeed |

## Notes

- Timezone semantics for schedule due checks and month-boundary attribution are fixed to AEST (UTC+10:00).
- Ordering and pagination for list endpoints must remain deterministic under local test runs.
- This quickstart intentionally excludes cloud deployment and multi-environment concerns.
