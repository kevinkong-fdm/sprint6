# Research: Digital Banking Access and Session Management

## Decision 1: Backend framework and language

- Decision: Use Java 21 with Spring Boot 3.3.x for backend implementation.
- Rationale: Spring Boot provides mature security, validation, observability, and transactional tooling suitable for banking authentication services.
- Alternatives considered: Node-based frameworks (faster prototyping but less aligned with requested stack lock), Micronaut/Quarkus (valid options but not requested), .NET (not requested).

## Decision 2: Required Spring Boot library set

- Decision: Require `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `spring-boot-starter-actuator`, `springdoc-openapi-starter-webmvc-ui`, `flyway-core`, `mysql-connector-j`, Argon2 library, and JWT library.
- Rationale: This set directly supports API delivery, schema validation, security policy enforcement, persistence, observability, and contract-first documentation.
- Alternatives considered: Minimal starter set with custom infrastructure code (rejected due to increased risk and divergence from guardrail intent).

## Decision 3: Forbidden Spring Boot patterns

- Decision: Forbid field injection, controller-layer business logic, exposing entities directly at API boundaries, exception swallowing, and unmanaged schema changes.
- Rationale: These patterns create maintainability and security risks and reduce testability in regulated domains.
- Alternatives considered: Team-style discretion by reviewer only (rejected because guardrails should be explicit and enforceable).

## Decision 4: Frontend framework and client strategy

- Decision: Use React 18 with a spec-first typed API client generated from OpenAPI.
- Rationale: A generated client enforces endpoint/schema consistency and reduces contract drift between frontend and backend.
- Alternatives considered: Manual endpoint wrappers (rejected due to drift risk), non-typed client layer (rejected due to weaker error handling and maintainability).

## Decision 5: Proper React error-handling architecture

- Decision: Centralize API error parsing/mapping to contract error codes, use error boundaries, and show deterministic UI fallbacks for network/4xx/5xx failures.
- Rationale: Explicit error strategy is required to deliver reliable user journeys and avoid silent failures in authentication UX.
- Alternatives considered: Local per-component ad-hoc error handling (rejected due to inconsistency and low observability).

## Decision 6: Forbidden React patterns

- Decision: Forbid direct ad-hoc `fetch` in presentation components, untyped `any` error payload handling, duplicated endpoint strings, and silent promise rejection.
- Rationale: These patterns undermine spec-first guarantees and produce brittle, inconsistent client behavior.
- Alternatives considered: Best-effort coding conventions only (rejected because explicit guardrails are required).

## Decision 7: Database platform

- Decision: Use MySQL 8.x as the only database for durable auth records and policy counters.
- Rationale: Satisfies explicit user request and keeps operational topology simple for the current increment.
- Alternatives considered: PostgreSQL plus Redis split (rejected because request constrains database to MySQL), MySQL plus Redis (rejected for same reason).

## Decision 8: Security model for credentials and sessions

- Decision: Use Argon2id password hashing, 60-minute access tokens, and 30-day rotating refresh tokens.
- Rationale: Aligns with approved specification clarifications and supports replay detection and controlled session continuity.
- Alternatives considered: Non-rotating refresh tokens (rejected due to weaker replay resistance), longer access tokens (rejected due to increased exposure window).

## Decision 9: Abuse controls and non-disclosure behavior

- Decision: Enforce lockout after 10 failed attempts for 15 minutes; throttle reset requests to 5/hour/email; return non-disclosing reset responses.
- Rationale: Meets clarified policy and provides deterministic, testable controls.
- Alternatives considered: Progressive delay only (rejected due to weaker deterministic testing), explicit unknown-email response (rejected due to enumeration risk).

## Decision 10: Contract-first quality gates

- Decision: OpenAPI remains the source of truth; backend and frontend must pass contract checks before promotion.
- Rationale: Protects spec alignment for both server and client paths.
- Alternatives considered: Code-first API then retrofit contract (rejected due to high drift risk and weaker SDD discipline).
