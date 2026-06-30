# Implementation Plan: Authenticated Standing Orders, Notifications, Statements, and Spending Insights

**Branch**: `004-standing-orders-insights` | **Date**: 2026-06-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/004-standing-orders-insights/spec.md`

## Summary

Deliver authenticated standing-order setup/lifecycle management, execution-trigger notifications, monthly statement generation, and spending insights under the current Spring Boot + React + MySQL stack. The design enforces clarified policy constraints: notifications are default-on and immutable in this version, spending insights return deterministic low-data success payloads when history is insufficient, schedule and month attribution use fixed AEST (UTC+10:00), and standing-order destination accounts are internal-only and must be owned by the same authenticated customer as the source account. This increment is scoped to local execution only.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.3.1) and TypeScript 5.x (React 18)

**Primary Dependencies**:
- Backend: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`, `springdoc-openapi-starter-webmvc-ui`, `flyway-core`, `flyway-mysql`, `mysql-connector-j`, `jjwt`, `argon2-jvm`
- Frontend: `react`, `react-router-dom`, `@tanstack/react-query`, `react-hook-form`, `zod`, `openapi-typescript-codegen`, `vite`

**Storage**: MySQL 8.x as the single local system of record, managed through Flyway migrations

**Testing**:
- Backend: JUnit 5, Spring Boot Test, Spring Security Test, Testcontainers MySQL
- Contract: OpenAPI-driven contract tests using the backend `contract` profile
- Frontend: Vitest + Testing Library for feature behavior and API integration

**Target Platform**: Local developer workstation only (Windows/macOS/Linux) running MySQL, backend API, and frontend SPA locally

**Project Type**: Web application (Spring Boot backend + React frontend)

**Performance Goals**:
- Standing-order execution outcomes processed within the local validation threshold from the spec (5 minutes)
- Statement generation responses complete within 10 seconds for local representative datasets
- Notification records become queryable within 1 minute of execution outcome creation in local runs

**Constraints**:
- Authentication and ownership authorization are mandatory for every operation
- Standing-order destinations are limited to internal platform account IDs owned by the same authenticated customer
- Platform timezone is fixed to AEST (UTC+10:00) for schedule due-time checks and month-boundary attribution
- Notifications remain fixed default-on and cannot be modified by customers
- Execution and posting logic must avoid partial financial state on failure
- Sensitive financial and identity data must not leak through logs, errors, statements, insights, or telemetry
- Project only needs to work locally for this iteration

**Scale/Scope**: Single-instance local deployment with representative test data; no cloud deployment requirements

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is a template placeholder with no enforceable project-specific principles. Temporary gates are therefore derived from the approved feature specification and repository practices:

1. Spec-first and contract-first execution is mandatory
2. Every endpoint remains authentication- and ownership-authorized
3. Error-code mapping remains deterministic and complete
4. Standing-order execution and related money movement remain atomic
5. AEST timezone semantics are consistent across scheduling and statements
6. Delivery remains local-only for this feature increment

**Pre-Phase-0 Gate Result**: PASS

**Post-Phase-1 Gate Re-Check**: PASS

## Project Structure

### Documentation (this feature)

```text
specs/004-standing-orders-insights/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── standing-orders-insights.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/banking/
│   │   │   ├── auth/
│   │   │   ├── customer/
│   │   │   ├── account/
│   │   │   ├── standingorder/
│   │   │   ├── notification/
│   │   │   ├── statement/
│   │   │   └── insights/
│   │   └── resources/
│   │       └── db/migration/
│   └── test/
│       └── java/com/example/banking/
│           ├── standingorder/
│           ├── notification/
│           ├── statement/
│           └── insights/
└── pom.xml

frontend/
├── src/
│   ├── api/
│   ├── app/
│   └── features/
│       ├── standing-orders/
│       ├── notifications/
│       ├── statements/
│       └── insights/
└── package.json

specs/
└── 004-standing-orders-insights/
```

**Structure Decision**: Keep the existing backend + frontend split, deliver backend-first OpenAPI contract updates for standing orders/notifications/statements/insights, and wire frontend integration to generated typed clients from the new feature contract.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
