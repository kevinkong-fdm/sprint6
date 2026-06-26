# Quickstart: Customer Profile Lifecycle Management

This feature is scoped for local execution only.

This guide validates the feature end-to-end against:
- Contract: [contracts/customer.openapi.yaml](./contracts/customer.openapi.yaml)
- Data model: [data-model.md](./data-model.md)
- Feature specification: [spec.md](./spec.md)

## Implemented Endpoint Map

- `POST /customers` -> create customer profile
- `GET /customers/{customerId}` -> retrieve customer profile
- `PATCH /customers/{customerId}` -> update mutable customer fields
- `DELETE /customers/{customerId}` -> hard-delete customer profile with cascade

## Prerequisites

- Java 21
- Maven 3.9+ (or project wrapper)
- MySQL 8.x
- Optional frontend runtime: Node.js 22 LTS

## Setup

1. Start MySQL.

Docker option:

```bash
docker compose up -d mysql
```

2. Ensure target database exists.

```sql
CREATE DATABASE IF NOT EXISTS banking_auth;
```

3. Configure backend datasource and auth settings.

Expected config source:
- `backend/src/main/resources/application-local.yml`

Required values:
- JDBC URL
- DB username/password
- JWT secret

4. Start backend service.

```bash
cd backend
mvn flyway:migrate
mvn spring-boot:run
```

Expected outcome:
- Backend starts successfully on localhost and exposes customer lifecycle endpoints for local testing.

## Validation Scenarios

### Scenario 1: Create customer

```bash
curl -i -X POST http://localhost:8080/customers \
  -H "Authorization: Bearer <operator-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "email":"alice@example.com",
    "givenName":"Alice",
    "familyName":"Ng",
    "phoneNumber":"+12065550123",
    "preferredLanguage":"en"
  }'
```

Expected outcome:
- HTTP 201
- Response includes `customerId`, `createdAt`, and `updatedAt`
- Record persisted in MySQL

### Scenario 2: Duplicate create rejection

Repeat the same create request with identical normalized email.

Expected outcome:
- HTTP 409
- Error code `CUST-CRT-002`

### Scenario 3: Get customer details

```bash
curl -i http://localhost:8080/customers/<customerId> \
  -H "Authorization: Bearer <operator-token>"
```

Expected outcome:
- HTTP 200 for existing customer
- HTTP 404 with `CUST-GET-001` for unknown customerId

### Scenario 4: Update customer (last-write-wins)

1. Send two valid update requests for the same customer in close sequence.
2. Fetch customer details after both requests complete.

Expected outcome:
- Both updates return success when valid.
- Persisted profile reflects the most recently accepted update.
- No partial state appears for failed validation updates.

### Scenario 5: Delete customer (hard delete + cascade)

```bash
curl -i -X DELETE http://localhost:8080/customers/<customerId> \
  -H "Authorization: Bearer <operator-token>"
```

Expected outcome:
- HTTP 204 on success
- Customer and dependent records are physically removed
- Subsequent GET returns HTTP 404 with `CUST-GET-001`
- If cascading dependency deletion fails, delete returns HTTP 409 with `CUST-DEL-002`

### Scenario 6: Authentication guardrail

Invoke any customer endpoint without a valid operator token.

Expected outcome:
- HTTP 401/403 (per security configuration)
- Error code `CUST-AUTH-001`

## Automated Checks

Run required quality gates:

```bash
cd backend
mvn test
mvn verify -Pcontract
```

Run customer-only contract checks:

```bash
cd backend
mvn test -Pcontract -Dtest='com.example.banking.customer.contract.*'
```

Optional frontend checks if a customer management UI was implemented:

```bash
cd ../frontend
npm run test
npm run build
```

Expected outcome:
- Backend tests pass
- Contract checks pass
- Frontend checks pass when applicable

## Traceability Checklist

- Scenarios map to user stories in [spec.md](./spec.md)
- API behavior aligns with [contracts/customer.openapi.yaml](./contracts/customer.openapi.yaml)
- Persistence behavior aligns with [data-model.md](./data-model.md)
