# Implementation Plan: Authenticated Bank Account Operations

**Branch**: `003-bank-account-operations` | **Date**: 2026-06-29 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-bank-account-operations/spec.md`

## Summary

Deliver authenticated bank account operations for create/retrieve/list/update/delete, deposit, withdraw, transfer, and transaction history under the existing Spring Boot + React + MySQL stack. The implementation follows clarified policy decisions (delete allowed with any available balance if no pending movement exists) and is explicitly scoped to local-only execution with no cloud deployment requirements in this increment.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.3.1) and TypeScript 5.x (React 18)

**Primary Dependencies**:
- Backend: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`, `springdoc-openapi-starter-webmvc-ui`, `flyway-core`, `flyway-mysql`, `mysql-connector-j`, `jjwt`, `argon2-jvm`
- Frontend: `react`, `react-router-dom`, `@tanstack/react-query`, `react-hook-form`, `zod`, OpenAPI client generation (`openapi-typescript-codegen`)

**Storage**: MySQL 8.x (single local system of record) with Flyway migrations

**Testing**:
- Backend: JUnit 5, Spring Boot Test, Spring Security Test, Testcontainers MySQL
- Contract: OpenAPI-based contract validation and backend contract test profile
- Frontend: Vitest + Testing Library for UI/API integration where applicable

**Target Platform**: Local developer workstation only (Windows/macOS/Linux) running backend, frontend, and MySQL locally

**Project Type**: Web application (backend API + frontend SPA)

**Performance Goals**:
- Match spec success criteria in local environment
- Account retrieval/list operations return complete responses within 3 seconds for local validation datasets
- Monetary operations remain deterministic and atomic

**Constraints**:
- Authentication is mandatory for all in-scope operations
- Authorization boundaries must prevent cross-account access
- Transfer operations must be atomic
- No sensitive financial/identity data in logs or error payloads
- Local-only operation is required; production hardening is out of scope for this plan

**Scale/Scope**: Local validation scope only; single-instance deployment with representative local test data

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is currently an unfilled template with no enforceable project-specific principles. Temporary gates are derived from the active specification and repository standards:

1. Spec-first and contract-first workflow maintained
2. All in-scope stories remain authentication-gated
3. Error-code mappings remain complete and deterministic
4. Atomicity preserved for transfer and deletion eligibility rules
5. Delivery remains local-only with no cloud environment assumptions

**Pre-Phase-0 Gate Result**: PASS

**Post-Phase-1 Gate Re-Check**: PASS

## Project Structure

### Documentation (this feature)

```text
specs/003-bank-account-operations/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── account.openapi.yaml
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
│       └── java/com/example/banking/
│           ├── auth/
│           └── customer/
└── pom.xml

frontend/
├── src/
│   ├── api/
│   ├── app/
│   └── features/
└── package.json

specs/
└── 003-bank-account-operations/
```

**Structure Decision**: Use the existing backend + frontend repository structure with backend-first API and contract delivery, then wire frontend integration paths against the generated account operation contract.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
