# Tasks: Customer Profile Lifecycle Management

**Input**: Design documents from `/specs/002-customer-profile-crud/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/customer.openapi.yaml, quickstart.md

**Tests**: Contract and integration tests are required by guardrails and quality gates in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create feature scaffolding and local configuration baselines.

- [X] T001 Create customer module package marker in backend/src/main/java/com/example/banking/customer/package-info.java
- [X] T002 Create customer test package marker in backend/src/test/java/com/example/banking/customer/package-info.java
- [X] T003 [P] Add local customer feature configuration placeholders in backend/src/main/resources/application-local.yml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Implement core persistence and shared customer infrastructure required by all user stories.

**Critical**: No user story implementation should start before this phase is complete.

- [X] T004 Create customer baseline schema migration in backend/src/main/resources/db/migration/V2__customer_profile_baseline.sql
- [X] T005 [P] Implement customer profile entity in backend/src/main/java/com/example/banking/customer/domain/CustomerProfileEntity.java
- [X] T006 [P] Implement customer address entity in backend/src/main/java/com/example/banking/customer/domain/CustomerAddressEntity.java
- [X] T007 [P] Implement customer contact preference entity in backend/src/main/java/com/example/banking/customer/domain/CustomerContactPreferenceEntity.java
- [X] T008 [P] Implement customer lifecycle event entity in backend/src/main/java/com/example/banking/customer/domain/CustomerLifecycleEventEntity.java
- [X] T009 [P] Implement customer profile repository in backend/src/main/java/com/example/banking/customer/infrastructure/CustomerProfileRepository.java
- [X] T010 [P] Implement customer address repository in backend/src/main/java/com/example/banking/customer/infrastructure/CustomerAddressRepository.java
- [X] T011 [P] Implement customer contact preference repository in backend/src/main/java/com/example/banking/customer/infrastructure/CustomerContactPreferenceRepository.java
- [X] T012 [P] Implement customer lifecycle event repository in backend/src/main/java/com/example/banking/customer/infrastructure/CustomerLifecycleEventRepository.java
- [X] T013 Implement shared customer domain exception hierarchy in backend/src/main/java/com/example/banking/customer/application/CustomerDomainException.java
- [X] T014 Implement customer exception handler and error-code mapping in backend/src/main/java/com/example/banking/customer/api/CustomerExceptionHandler.java
- [X] T015 [P] Implement shared customer response DTO in backend/src/main/java/com/example/banking/customer/api/dto/CustomerResponse.java
- [X] T016 Implement lifecycle event service for CRUD audit events in backend/src/main/java/com/example/banking/customer/application/CustomerLifecycleEventService.java

**Checkpoint**: Foundational platform ready for independent user story delivery.

---

## Phase 3: User Story 1 - Create Customer (Priority: P1) 🎯 MVP

**Goal**: Allow authenticated operators to create a customer profile with validation and duplicate protection.

**Independent Test**: Submit valid create data and verify a unique customer profile is created; submit invalid/duplicate payloads and verify contract error codes.

### Tests for User Story 1

- [X] T017 [P] [US1] Add create-customer contract test in backend/src/test/java/com/example/banking/customer/contract/CreateCustomerContractTest.java
- [X] T018 [P] [US1] Add create-customer integration test in backend/src/test/java/com/example/banking/customer/integration/CreateCustomerIntegrationTest.java

### Implementation for User Story 1

- [X] T019 [P] [US1] Implement create request DTO in backend/src/main/java/com/example/banking/customer/api/dto/CustomerCreateRequest.java
- [X] T020 [P] [US1] Implement customer email normalizer in backend/src/main/java/com/example/banking/customer/application/CustomerEmailNormalizer.java
- [X] T021 [US1] Implement create customer service in backend/src/main/java/com/example/banking/customer/application/CreateCustomerService.java
- [X] T022 [US1] Implement create customer endpoint in backend/src/main/java/com/example/banking/customer/api/CreateCustomerController.java
- [X] T023 [US1] Add create validation and duplicate error mappings in backend/src/main/java/com/example/banking/customer/api/CustomerExceptionHandler.java
- [X] T024 [US1] Emit create lifecycle events from backend/src/main/java/com/example/banking/customer/application/CreateCustomerService.java

**Checkpoint**: User Story 1 is independently testable and demo-ready.

---

## Phase 4: User Story 2 - Get Customer Details (Priority: P1)

**Goal**: Allow authenticated operators to retrieve a complete customer profile by identifier.

**Independent Test**: Retrieve an existing customer and verify full contract response; retrieve unknown customer and verify not-found error code.

### Tests for User Story 2

- [X] T025 [P] [US2] Add get-customer contract test in backend/src/test/java/com/example/banking/customer/contract/GetCustomerContractTest.java
- [X] T026 [P] [US2] Add get-customer integration test in backend/src/test/java/com/example/banking/customer/integration/GetCustomerIntegrationTest.java

### Implementation for User Story 2

- [X] T027 [US2] Implement get customer query service in backend/src/main/java/com/example/banking/customer/application/GetCustomerService.java
- [X] T028 [US2] Implement customer response mapper in backend/src/main/java/com/example/banking/customer/api/dto/CustomerResponseMapper.java
- [X] T029 [US2] Implement get customer endpoint in backend/src/main/java/com/example/banking/customer/api/GetCustomerController.java
- [X] T030 [US2] Emit get lifecycle events from backend/src/main/java/com/example/banking/customer/application/GetCustomerService.java

**Checkpoint**: User Story 2 is independently testable and operational for service workflows.

---

## Phase 5: User Story 3 - Update Customer Profile (Priority: P2)

**Goal**: Allow authenticated operators to update mutable customer fields with deterministic last-write-wins behavior.

**Independent Test**: Submit valid updates and verify persisted changes; submit immutable/invalid updates and verify contract errors and unchanged persisted state.

### Tests for User Story 3

- [X] T031 [P] [US3] Add update-customer contract test in backend/src/test/java/com/example/banking/customer/contract/UpdateCustomerContractTest.java
- [X] T032 [P] [US3] Add update-customer integration and concurrency test in backend/src/test/java/com/example/banking/customer/integration/UpdateCustomerIntegrationTest.java

### Implementation for User Story 3

- [X] T033 [P] [US3] Implement update request DTO in backend/src/main/java/com/example/banking/customer/api/dto/CustomerUpdateRequest.java
- [X] T034 [P] [US3] Implement immutable field validator in backend/src/main/java/com/example/banking/customer/application/ImmutableCustomerFieldValidator.java
- [X] T035 [US3] Implement update customer service with last-write-wins semantics in backend/src/main/java/com/example/banking/customer/application/UpdateCustomerService.java
- [X] T036 [US3] Implement update customer endpoint in backend/src/main/java/com/example/banking/customer/api/UpdateCustomerController.java
- [X] T037 [US3] Add update validation and immutable-field error mappings in backend/src/main/java/com/example/banking/customer/api/CustomerExceptionHandler.java
- [X] T038 [US3] Emit update lifecycle events from backend/src/main/java/com/example/banking/customer/application/UpdateCustomerService.java

**Checkpoint**: User Story 3 is independently testable with deterministic concurrency behavior.

---

## Phase 6: User Story 4 - Delete Customer (Priority: P2)

**Goal**: Allow authenticated operators to hard-delete a customer and cascade-delete dependent records atomically.

**Independent Test**: Delete existing customer with dependencies and verify physical removal; force cascade failure and verify rollback with contract error code.

### Tests for User Story 4

- [X] T039 [P] [US4] Add delete-customer contract test in backend/src/test/java/com/example/banking/customer/contract/DeleteCustomerContractTest.java
- [X] T040 [P] [US4] Add delete-customer integration and rollback test in backend/src/test/java/com/example/banking/customer/integration/DeleteCustomerIntegrationTest.java

### Implementation for User Story 4

- [X] T041 [P] [US4] Implement cascade delete coordinator in backend/src/main/java/com/example/banking/customer/application/CustomerCascadeDeleteCoordinator.java
- [X] T042 [US4] Implement hard delete service with transactional rollback in backend/src/main/java/com/example/banking/customer/application/DeleteCustomerService.java
- [X] T043 [US4] Implement delete customer endpoint in backend/src/main/java/com/example/banking/customer/api/DeleteCustomerController.java
- [X] T044 [US4] Add delete not-found and cascade-failure error mappings in backend/src/main/java/com/example/banking/customer/api/CustomerExceptionHandler.java
- [X] T045 [US4] Emit delete lifecycle events from backend/src/main/java/com/example/banking/customer/application/DeleteCustomerService.java

**Checkpoint**: User Story 4 is independently testable with deterministic hard-delete and cascade behavior.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final consistency, documentation, and local validation hardening across stories.

- [X] T046 [P] Update local validation walkthrough for implemented endpoints in specs/002-customer-profile-crud/quickstart.md
- [X] T047 [P] Update contract-to-test traceability notes in specs/002-customer-profile-crud/spec.md
- [X] T048 Configure customer contract test profile execution in backend/pom.xml

---

## Phase 8: Frontend Integration - Authenticated Customer Console

**Purpose**: Connect post-login frontend flow to authenticated customer CRUD operations while preserving current styling and UX patterns.

- [X] T049 Implement JWT bearer request authentication filter and register it in backend/src/main/java/com/example/banking/auth/config/SecurityConfig.java and backend/src/main/java/com/example/banking/auth/config/JwtAuthenticationFilter.java
- [X] T050 [US5] Add auth session persistence utilities in frontend/src/features/auth/session/AuthSessionContext.tsx
- [X] T051 [US5] Add customer API client and error mapper in frontend/src/features/customer/api/customers.ts and frontend/src/features/customer/api/errorMapper.ts
- [X] T052 [US5] Implement customer operations page UI in frontend/src/features/customer/pages/CustomersPage.tsx
- [X] T053 [US5] Wire protected customer routing and login redirect in frontend/src/app/routes.tsx, frontend/src/app/AppShell.tsx, frontend/src/features/auth/pages/LoginPage.tsx, and frontend/src/main.tsx
- [X] T054 [US5] Add frontend login-to-customers integration test in frontend/tests/auth/login-to-customers.test.tsx
- [X] T055 [US5] Run frontend quality checks (`npm run test`, `npm run build`) from frontend/

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1) has no dependencies and can start immediately.
- Foundational (Phase 2) depends on Setup and blocks all user stories.
- User story phases (Phase 3 through Phase 6) depend on Foundational completion.
- Polish (Phase 7) depends on completion of all selected user stories.

### User Story Dependencies

- US1 (Create Customer): can start immediately after Foundational.
- US2 (Get Customer Details): can start immediately after Foundational and remains independently testable.
- US3 (Update Customer Profile): depends on foundational models and benefits from US1 create flow for test fixtures.
- US4 (Delete Customer): depends on foundational models and benefits from US1 create flow for delete target fixtures.

### Dependency Graph

- Phase 1 -> Phase 2 -> {Phase 3, Phase 4} -> {Phase 5, Phase 6} -> Phase 7

---

## Parallel Opportunities

- Setup parallel task: T003.
- Foundational parallel tasks: T005, T006, T007, T008, T009, T010, T011, T012, T015.
- US1 parallel tasks: T017, T018, T019, T020.
- US2 parallel tasks: T025, T026.
- US3 parallel tasks: T031, T032, T033, T034.
- US4 parallel tasks: T039, T040, T041.

### Parallel Example: User Story 1

- Run T017 and T018 together while preparing failing tests.
- Run T019 and T020 together before starting T021.

### Parallel Example: User Story 3

- Run T031 and T032 together while preparing failing tests.
- Run T033 and T034 together before starting T035.

---

## Implementation Strategy

### MVP First (US1)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 (US1).
3. Validate US1 independently with contract and integration tests.
4. Demo local create workflow before expanding scope.

### Incremental Delivery

1. Deliver US1 (create) as MVP.
2. Deliver US2 (get) for operational servicing completeness.
3. Deliver US3 (update) with deterministic concurrency behavior.
4. Deliver US4 (delete) with atomic cascade semantics.
5. Complete Phase 7 polish and finalize local quickstart validation.

### Team Parallelization Strategy

1. Team completes Phase 1 and Phase 2 together.
2. One stream handles US1 while another handles US2.
3. After US1/US2 stabilize, split streams between US3 and US4.
4. Rejoin for Phase 7 documentation and release checks.

---

## Notes

- All tasks follow the required checklist format: checkbox, task ID, optional [P], optional [US#], and explicit file path.
- User story tasks include [US#] labels for traceability.
- Tests are listed before implementation in each user story phase and are expected to fail first.
