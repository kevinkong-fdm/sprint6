# Quickstart: Digital Banking Access and Session Management

This guide validates the feature end-to-end against:
- Contract: [contracts/auth.openapi.yaml](./contracts/auth.openapi.yaml)
- Data model: [data-model.md](./data-model.md)
- Feature specification: [spec.md](./spec.md)

## Prerequisites

- Java 21
- Maven 3.9+ (or project wrapper)
- Node.js 22 LTS
- MySQL 8.x

## Setup

1. Create and configure the MySQL database.

```sql
CREATE DATABASE banking_auth;
```

2. Configure backend environment (example in `backend/src/main/resources/application-local.yml`).

Required values:
- MySQL JDBC URL
- DB username and password
- JWT signing/verification keys

3. Run backend migrations and start Spring Boot API.

```bash
cd backend
./mvnw flyway:migrate
./mvnw spring-boot:run
```

4. Install frontend dependencies and generate typed API client from OpenAPI.

```bash
cd ../frontend
npm ci
npm run generate:api
```

5. Start React client.

```bash
npm run dev
```

Expected outcome:
- Spring Boot API starts successfully and serves contract-documented endpoints.
- React app starts and uses generated typed API client.

## Validation Scenarios

### Scenario 1: Register a new account

```bash
curl -i -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"StrongPass!234"}'
```

Expected outcome:
- HTTP 201 response.
- Response matches contract schema.
- Account persisted in MySQL with normalized email and hashed password.

### Scenario 2: Duplicate registration handling in API and React

Repeat registration with the same email, then attempt the same flow in React UI.

Expected outcome:
- API returns HTTP 409 with `AUTH-REG-002`.
- React displays mapped, user-safe error message via centralized error handler.

### Scenario 3: Login and lockout policy

1. Submit valid login request; verify token lifetimes.
2. Submit invalid password 10 times, then one additional attempt.

Expected outcome:
- Successful login returns 60-minute access token and 30-day refresh token metadata.
- Additional attempt during lockout returns HTTP 423 with `AUTH-LOGIN-002`.

### Scenario 4: Password reset request non-disclosure and throttle policy

1. Submit reset request for known email and unknown email.
2. Submit six reset requests for same known email within one hour.

Expected outcome:
- Known and unknown email outward responses remain non-disclosing.
- Sixth request returns HTTP 429 with `AUTH-RESET-002`.
- Reset link validity enforced at 30 minutes.

### Scenario 5: Token refresh and replay protection

1. Refresh with valid refresh token.
2. Reuse previously rotated refresh token.

Expected outcome:
- First refresh returns new token set.
- Reuse attempt returns HTTP 401 with `AUTH-TOKEN-002` and records security event.

### Scenario 6: React spec-first client and error boundary behavior

1. Verify all auth calls in React are made via generated typed API client.
2. Simulate backend 5xx and network timeout.

Expected outcome:
- No ad-hoc direct `fetch` calls from view components.
- Global/route error boundaries render deterministic fallback UI.
- Error telemetry captures correlation ID when available.

## Automated Checks

Run backend and frontend quality gates:

```bash
cd backend
./mvnw test
./mvnw verify -Pcontract

cd ../frontend
npm run test
```

Expected outcome:
- Backend unit/integration tests pass.
- Frontend tests pass with error-handling flows covered.
- Contract tests confirm no undocumented fields and correct error-code mappings.

## Traceability Checklist

- Scenarios map to user stories in [spec.md](./spec.md).
- Request and response shapes conform to [contracts/auth.openapi.yaml](./contracts/auth.openapi.yaml).
- Persistence behavior aligns with MySQL-backed entities in [data-model.md](./data-model.md).
