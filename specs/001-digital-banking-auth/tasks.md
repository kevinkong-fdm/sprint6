# Tasks: Digital Banking Access and Session Management

**Input**: Design documents from /specs/001-digital-banking-auth/

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/auth.openapi.yaml, quickstart.md

**Tests**: Test tasks are included because the specification requires contract validation and quality gates.

**Organization**: Tasks are grouped by user story so each story can be implemented and tested independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize Spring Boot backend, React frontend, and MySQL local runtime.

- [X] T001 Create Spring Boot project skeleton and module metadata in backend/pom.xml
- [X] T002 Create React project skeleton and scripts in frontend/package.json
- [X] T003 [P] Configure required Spring Boot dependencies in backend/pom.xml
- [X] T004 [P] Configure required React dependencies in frontend/package.json
- [X] T005 [P] Provision local MySQL for development in docker-compose.yml (optional if MySQL is already installed/running on host)
- [X] T006 Configure backend local environment for MySQL/JWT in backend/src/main/resources/application-local.yml (or backend/src/main/resources/application.yml for single-profile local setup)
- [X] T007 [P] Create frontend environment template in frontend/.env.example
- [X] T008 Configure OpenAPI client generation script in frontend/openapi-config.ts

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core platform capabilities required before implementing any user story.

**CRITICAL**: No user story tasks start before this phase is complete.

- [X] T009 Create Flyway baseline migration for auth tables in backend/src/main/resources/db/migration/V1__auth_baseline.sql
- [X] T010 [P] Create UserAccount JPA entity in backend/src/main/java/com/example/banking/auth/domain/UserAccountEntity.java
- [X] T011 [P] Create CredentialRecord JPA entity in backend/src/main/java/com/example/banking/auth/domain/CredentialRecordEntity.java
- [X] T012 [P] Create AuthenticationEvent JPA entity in backend/src/main/java/com/example/banking/auth/domain/AuthenticationEventEntity.java
- [X] T013 Implement standardized error response model in backend/src/main/java/com/example/banking/auth/api/ErrorResponse.java
- [X] T014 Implement global exception handling and error code mapping in backend/src/main/java/com/example/banking/auth/api/GlobalExceptionHandler.java
- [X] T015 Implement Spring Security configuration in backend/src/main/java/com/example/banking/auth/config/SecurityConfig.java
- [X] T016 Implement Argon2 password hashing service in backend/src/main/java/com/example/banking/auth/application/PasswordHashService.java
- [X] T017 Implement JWT token service for access/refresh issuance in backend/src/main/java/com/example/banking/auth/application/JwtTokenService.java
- [X] T018 Implement correlation ID request filter in backend/src/main/java/com/example/banking/auth/logging/CorrelationIdFilter.java
- [X] T019 [P] Implement generated API client wrapper in frontend/src/api/client.ts
- [X] T020 [P] Implement centralized frontend API error mapper in frontend/src/features/auth/api/errorMapper.ts

**Checkpoint**: Foundation complete. User story implementation can begin.

---

## Phase 3: User Story 1 - User Registration (Priority: P1)

**Goal**: Allow a new customer to register with email and password.

**Independent Test**: Submit valid registration and verify account creation; submit duplicate email and invalid payload and verify mapped errors.

### Tests for User Story 1

- [X] T021 [P] [US1] Create failing contract test for POST /auth/register in backend/src/test/java/com/example/banking/auth/contract/RegisterContractTest.java
- [X] T022 [P] [US1] Create failing integration test for registration validation and duplicate email in backend/src/test/java/com/example/banking/auth/integration/RegistrationFlowIntegrationTest.java

### Implementation for User Story 1

- [X] T023 [P] [US1] Create registration request DTO in backend/src/main/java/com/example/banking/auth/api/dto/RegisterRequest.java
- [X] T024 [P] [US1] Create registration response DTO in backend/src/main/java/com/example/banking/auth/api/dto/RegisterResponse.java
- [X] T025 [US1] Implement registration business rules service in backend/src/main/java/com/example/banking/auth/application/RegistrationService.java
- [X] T026 [US1] Implement registration endpoint controller in backend/src/main/java/com/example/banking/auth/api/AuthRegistrationController.java
- [X] T027 [US1] Persist registration security events in backend/src/main/java/com/example/banking/auth/infrastructure/AuthenticationEventRepository.java
- [X] T028 [US1] Implement React registration API adapter in frontend/src/features/auth/api/register.ts
- [X] T029 [US1] Implement React registration form with mapped errors in frontend/src/features/auth/components/RegistrationForm.tsx
- [X] T030 [US1] Add frontend registration flow test in frontend/tests/auth/registration-flow.test.tsx

**Checkpoint**: User Story 1 is independently functional and testable.

---

## Phase 4: User Story 2 - User Login (Authentication) (Priority: P1)

**Goal**: Authenticate a registered user with email and password, enforcing lockout and issuing tokens.

**Independent Test**: Verify login success, invalid-credential failure, and 10-attempt lockout behavior with 15-minute lockout duration.

### Tests for User Story 2

- [X] T031 [P] [US2] Create failing contract test for POST /auth/login in backend/src/test/java/com/example/banking/auth/contract/LoginContractTest.java
- [X] T032 [P] [US2] Create failing integration test for lockout policy in backend/src/test/java/com/example/banking/auth/integration/LoginLockoutIntegrationTest.java

### Implementation for User Story 2

- [X] T033 [P] [US2] Create login request DTO in backend/src/main/java/com/example/banking/auth/api/dto/LoginRequest.java
- [X] T034 [US2] Implement login-attempt counter repository in backend/src/main/java/com/example/banking/auth/infrastructure/LoginAttemptCounterRepository.java
- [X] T035 [US2] Implement login service with lockout and token issuance in backend/src/main/java/com/example/banking/auth/application/LoginService.java
- [X] T036 [US2] Implement login endpoint controller in backend/src/main/java/com/example/banking/auth/api/AuthLoginController.java
- [X] T037 [US2] Implement login success/failure audit logging in backend/src/main/java/com/example/banking/auth/application/LoginAuditService.java
- [X] T038 [US2] Implement React login API adapter in frontend/src/features/auth/api/login.ts
- [X] T039 [US2] Implement React login page with lockout UX in frontend/src/features/auth/pages/LoginPage.tsx
- [X] T040 [US2] Add frontend login lockout/error test in frontend/tests/auth/login-lockout.test.tsx

**Checkpoint**: User Stories 1 and 2 are independently functional and testable.

---

## Phase 5: User Story 3 - Password Reset Request (Priority: P2)

**Goal**: Accept password reset requests with non-disclosing responses and enforce reset throttling.

**Independent Test**: Verify neutral response for known and unknown emails, throttle on sixth request per hour, and 30-minute reset link validity metadata.

### Tests for User Story 3

- [X] T041 [P] [US3] Create failing contract test for POST /auth/password-reset/request in backend/src/test/java/com/example/banking/auth/contract/PasswordResetRequestContractTest.java
- [X] T042 [P] [US3] Create failing integration test for reset non-disclosure and throttling in backend/src/test/java/com/example/banking/auth/integration/PasswordResetThrottleIntegrationTest.java

### Implementation for User Story 3

- [X] T043 [P] [US3] Create password reset request entity in backend/src/main/java/com/example/banking/auth/domain/PasswordResetRequestEntity.java
- [X] T044 [P] [US3] Create password reset throttle counter entity in backend/src/main/java/com/example/banking/auth/domain/PasswordResetThrottleCounterEntity.java
- [X] T045 [US3] Implement password reset request service with 5-per-hour and 30-minute policy in backend/src/main/java/com/example/banking/auth/application/PasswordResetRequestService.java
- [X] T046 [US3] Implement password reset request endpoint controller in backend/src/main/java/com/example/banking/auth/api/PasswordResetController.java
- [X] T047 [US3] Implement reset-request audit logging in backend/src/main/java/com/example/banking/auth/application/PasswordResetAuditService.java
- [X] T048 [US3] Implement React reset request API adapter in frontend/src/features/auth/api/passwordResetRequest.ts
- [X] T049 [US3] Implement React reset request form with neutral messaging in frontend/src/features/auth/components/PasswordResetRequestForm.tsx
- [X] T050 [US3] Add frontend reset-request throttle test in frontend/tests/auth/password-reset-throttle.test.tsx

**Checkpoint**: User Story 3 is independently functional and testable.

---

## Phase 6: User Story 4 - Token Refresh (Priority: P2)

**Goal**: Rotate refresh tokens, issue new token sets, and detect token reuse.

**Independent Test**: Verify refresh success with rotation, expiry rejection, and reused-token rejection with security event.

### Tests for User Story 4

- [X] T051 [P] [US4] Create failing contract test for POST /auth/token/refresh in backend/src/test/java/com/example/banking/auth/contract/TokenRefreshContractTest.java
- [X] T052 [P] [US4] Create failing integration test for refresh rotation and reuse detection in backend/src/test/java/com/example/banking/auth/integration/TokenRefreshRotationIntegrationTest.java

### Implementation for User Story 4

- [X] T053 [P] [US4] Create refresh token session entity in backend/src/main/java/com/example/banking/auth/domain/RefreshTokenSessionEntity.java
- [X] T054 [US4] Implement refresh token session repository in backend/src/main/java/com/example/banking/auth/infrastructure/RefreshTokenSessionRepository.java
- [X] T055 [US4] Implement refresh token service with rotation and reuse detection in backend/src/main/java/com/example/banking/auth/application/RefreshTokenService.java
- [X] T056 [US4] Implement token refresh endpoint controller in backend/src/main/java/com/example/banking/auth/api/TokenRefreshController.java
- [X] T057 [US4] Implement token reuse security event publisher in backend/src/main/java/com/example/banking/auth/application/TokenSecurityEventService.java
- [X] T058 [US4] Implement React refresh-token adapter and interceptor wiring in frontend/src/features/auth/api/refreshToken.ts
- [X] T059 [US4] Add frontend token refresh expiry/failure test in frontend/tests/auth/token-refresh-failure.test.tsx

**Checkpoint**: User Story 4 is independently functional and testable.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Complete cross-story quality, security, and delivery hardening.

- [X] T060 [P] Document auth API usage and stack constraints in docs/auth/overview.md
- [X] T061 [P] Add CI workflow for backend, frontend, and contract gates in .github/workflows/auth-ci.yml
- [X] T062 Add forbidden-pattern verification checklist in docs/security/forbidden-patterns-checklist.md
- [X] T063 Run and fix backend contract profile failures in backend/pom.xml
- [X] T064 Validate and update execution steps in specs/001-digital-banking-auth/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1): Can start immediately.
- Foundational (Phase 2): Depends on Setup completion and blocks all user stories.
- User Stories (Phases 3-6): Depend on Foundational completion.
- Polish (Phase 7): Depends on target user stories being complete.

### User Story Dependencies

- US1: Starts after Phase 2; no dependency on other stories.
- US2: Starts after Phase 2; no hard dependency on US1.
- US3: Starts after Phase 2; no hard dependency on US1/US2.
- US4: Starts after Phase 2; may reuse login token issuance components but remains independently testable with seeded session fixtures.

### Within Each User Story

- Contract and integration tests are created first and should fail before implementation tasks.
- DTO/entity tasks precede service tasks.
- Service tasks precede endpoint/controller tasks.
- Backend API completion precedes frontend integration tasks.

---

## Parallel Opportunities

- Setup parallel tasks: T003, T004, T005, T007.
- Foundational parallel tasks: T010, T011, T012, T019, T020.
- Per-story parallel tests: T021/T022, T031/T032, T041/T042, T051/T052.
- Per-story parallel model tasks: T023/T024, T043/T044, T053.
- Cross-story parallelism after Phase 2: US1-US4 can be developed by different developers with coordination on shared files.

---

## Parallel Example: User Story 1

- Run T021 and T022 in parallel while both remain failing.
- Run T023 and T024 in parallel after test scaffolds are in place.
- Once T025 and T026 complete, run T028 and T029 in parallel with backend stabilization.

## Parallel Example: User Story 2

- Run T031 and T032 in parallel.
- Run T038 and T039 in parallel after T036 provides endpoint behavior.

## Parallel Example: User Story 3

- Run T041 and T042 in parallel.
- Run T043 and T044 in parallel.
- Run T048 and T049 in parallel after T046 stabilizes API behavior.

## Parallel Example: User Story 4

- Run T051 and T052 in parallel.
- Run T058 and T059 in parallel after T056 stabilizes API behavior.

---

## Implementation Strategy

### MVP First (Suggested)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate US1 independently before continuing.

### Incremental Delivery

1. Deliver US1, then US2, then US3, then US4.
2. Validate each story independently before moving forward.
3. Keep contract tests and frontend error-handling checks green at each increment.

### Parallel Team Strategy

1. Team completes Phase 1 and Phase 2 together.
2. After Phase 2, split by stories:
   - Developer A: US1
   - Developer B: US2
   - Developer C: US3
   - Developer D: US4
3. Merge story branches after each independent checkpoint passes.

---

## Notes

- All tasks follow the required checklist format with explicit IDs and file paths.
- [P] marks tasks that can run in parallel.
- [US#] labels map tasks to user stories for traceability.
- Avoid introducing out-of-contract fields or forbidden backend/frontend patterns.
