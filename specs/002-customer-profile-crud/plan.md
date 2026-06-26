# Implementation Plan: Customer Profile Lifecycle Management

**Branch**: `002-customer-profile-crud` | **Date**: 2026-06-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-customer-profile-crud/spec.md`

## Summary

Deliver customer profile lifecycle APIs for create, get, update, and delete under the current platform stack (Spring Boot + React + MySQL) for local-only execution. The feature follows clarified policy decisions: any authenticated operator can perform full CRUD, concurrent updates use deterministic last-write-wins behavior, and delete performs irreversible hard delete with cascading dependent-record deletion. Delivery remains spec-first and contract-first with strict error-code mapping and auditable lifecycle events.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.3.1) and TypeScript 5.x (React 18)

**Primary Dependencies**:
- Backend: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`, `springdoc-openapi-starter-webmvc-ui`, `flyway-core`, `flyway-mysql`, `mysql-connector-j`
- Frontend (if customer admin UI wiring is included): `react`, `react-dom`, `react-router-dom`, `@tanstack/react-query`, `react-hook-form`, `zod`, OpenAPI-generated typed client

**Storage**: MySQL 8.4 (single system of record for customer profiles and dependent records)

**Testing**:
- Backend: JUnit 5, Spring Boot Test, Testcontainers MySQL
- Contract: OpenAPI validation + backend contract tests
- Frontend: Vitest + React Testing Library (only if feature includes UI integration)

**Target Platform**: Local developer workstation (Windows/macOS/Linux) running backend, frontend, and MySQL locally

**Project Type**: Web application with backend-first API delivery

**Performance Goals**: Meet feature success criteria in local runs (including retrieval under 3 seconds), with no production SLA target in this phase

**Constraints**:
- Hard delete with cascading dependent-record deletion must be atomic and deterministic
- Concurrent valid updates must follow last-write-wins semantics
- All operations require authenticated operator context
- No sensitive customer data in logs, error payloads, or telemetry
- Feature must be runnable end-to-end on a single local machine without cloud-managed services

**Scale/Scope**: Local development and validation scope only; single-instance execution with representative test data

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is currently a template placeholder with no enforceable project-specific principles. Temporary gates are therefore derived from the approved feature specification and existing repository constraints:

1. Spec-first and contract-first workflow is maintained
2. Clarified policy decisions are reflected consistently in design artifacts
3. Stack remains within current repository standards (Spring Boot + React + MySQL)
4. Error-code and negative-scenario traceability is preserved
5. Security guardrails (authentication required, no sensitive logging) remain enforceable
6. Delivery is local-only with no production deployment assumptions

**Pre-Phase-0 Gate Result**: PASS

**Post-Phase-1 Gate Re-Check**: PASS

## Project Structure

### Documentation (this feature)

```text
specs/002-customer-profile-crud/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── customer.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/banking/
│   │   │   ├── auth/
│   │   │   └── customer/
│   │   └── resources/
│   │       └── db/migration/
│   └── test/
│       └── java/com/example/banking/customer/
└── pom.xml

frontend/
├── src/
│   ├── app/
│   ├── api/
│   └── features/
│       └── customer/
└── tests/

tests/
└── contract/
    └── customer/
```

**Structure Decision**: Use the existing web application layout with backend-first implementation. Customer CRUD domain components are added under backend service boundaries first; frontend customer admin integration remains optional and only proceeds when backed by completed contract operations.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
