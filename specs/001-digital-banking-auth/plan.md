# Implementation Plan: Digital Banking Access and Session Management

**Branch**: `001-digital-banking-auth` | **Date**: 2026-06-25 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-digital-banking-auth/spec.md`

## Summary

Deliver a secure authentication foundation for digital banking with four API journeys: registration, login, password-reset request, and token refresh. Implementation is constrained to a Spring Boot backend, a React client, and MySQL as the only database. Delivery remains spec-first and contract-first, with strict policy alignment: email-only identity, no MFA in v1, 60-minute access token lifetime, 30-day refresh lifetime, lockout after 10 failed attempts for 15 minutes, and reset throttling at 5 requests/hour with 30-minute reset-link validity.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.3.x) and TypeScript 5.x (React 18)

**Primary Dependencies**:
- Backend required libraries: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`, `springdoc-openapi-starter-webmvc-ui`, `argon2-jvm`, JWT library (`jjwt` or Nimbus JOSE), `flyway-core`, `mysql-connector-j`
- Frontend required libraries: `react`, `react-dom`, `react-router-dom`, typed API client generated from OpenAPI, query/state library (`@tanstack/react-query`), form/schema validation (`react-hook-form` + `zod`)

**Storage**: MySQL 8.x only (durable records plus security counter persistence; no Redis)

**Testing**:
- Backend: JUnit 5, Spring Boot Test, MockMvc or REST Assured, Testcontainers MySQL
- Frontend: Vitest, React Testing Library, MSW
- Contract: OpenAPI schema validation and contract tests against backend endpoints

**Target Platform**: Linux container runtime (cloud-hosted web application)

**Project Type**: Web application (Spring Boot backend + React frontend)

**Performance Goals**: p95 <= 300ms for auth API endpoints under nominal load; p99 <= 600ms excluding external email transport latency

**Constraints**:
- Spring Boot required patterns: constructor injection, service-layer transaction boundaries, DTO-based controller contracts, centralized exception handling, Flyway-managed schema changes
- Spring Boot forbidden patterns: field injection, controller business logic, exposing JPA entities directly from controllers, swallowing generic exceptions, raw/native SQL without explicit justification
- React required patterns: OpenAPI-spec-first typed client usage, centralized API error mapping from contract error schema, error boundaries and route-level fallback UI, deterministic handling of network and 4xx/5xx failures
- React forbidden patterns: direct ad-hoc `fetch` calls in view components, untyped `any` error handling, silent promise failures, endpoint strings duplicated outside typed API client layer
- Security constraints: no plaintext logging of passwords/tokens/secrets; non-disclosing reset responses

**Scale/Scope**: Initial rollout target of up to 100k registered users, 10k daily active users, and peak bursts of 200 auth requests/second

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is still a template placeholder with no enforceable project-specific principles. Temporary mandatory gates are therefore derived from the approved specification and this technology constraint update:

1. Stack lock: Spring Boot + React + MySQL only
2. Spec-first and contract-first execution
3. Security controls are encoded as testable requirements
4. No out-of-contract fields in request/response payloads
5. Contract tests are required for merge and promotion readiness

**Pre-Phase-0 Gate Result**: PASS

**Post-Phase-1 Gate Re-Check**: PASS

## Project Structure

### Documentation (this feature)

```text
specs/001-digital-banking-auth/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── auth.openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/banking/auth/
│   │   └── resources/
│   └── test/
│       └── java/
└── pom.xml

frontend/
├── src/
│   ├── app/
│   ├── pages/
│   ├── features/auth/
│   ├── api/
│   └── components/
├── tests/
└── package.json

tests/
└── contract/
    └── auth/
```

**Structure Decision**: Web application structure selected to explicitly support Spring Boot backend and React client collaboration under a shared OpenAPI contract.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
