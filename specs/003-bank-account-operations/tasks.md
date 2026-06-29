# Tasks: Authenticated Bank Account Operations

**Input**: Design documents from `/specs/003-bank-account-operations/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/account.openapi.yaml, quickstart.md

**Tests**: Contract and integration tests are REQUIRED for this feature per plan/research local quality gates.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize feature scaffolding and local tooling hooks for account operations.

- [X] T001 Update account OpenAPI generation script in frontend/package.json
- [X] T002 Create account domain package marker in backend/src/main/java/com/example/banking/account/package-info.java
- [X] T003 [P] Create account contract test package marker in backend/src/test/java/com/example/banking/account/contract/package-info.java
- [X] T004 [P] Create account integration test package marker in backend/src/test/java/com/example/banking/account/integration/package-info.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build core persistence, security, authorization, and shared account primitives required by all stories.

**CRITICAL**: No user story implementation starts before this phase is complete.

- [X] T005 Create account schema migration for accounts, movements, delete audits, and indexes in backend/src/main/resources/db/migration/V4__account_operations_baseline.sql
- [X] T006 [P] Implement account core enums and entities in backend/src/main/java/com/example/banking/account/domain/
- [X] T007 [P] Implement account repositories in backend/src/main/java/com/example/banking/account/infrastructure/
- [X] T008 [P] Implement account domain exception hierarchy and error-code mapping in backend/src/main/java/com/example/banking/account/application/AccountDomainException.java
- [X] T009 Implement standardized account exception handler in backend/src/main/java/com/example/banking/account/api/AccountExceptionHandler.java
- [X] T010 Implement authenticated account-ownership resolver in backend/src/main/java/com/example/banking/account/application/AccountAuthorizationService.java
- [X] T011 [P] Implement movement idempotency service in backend/src/main/java/com/example/banking/account/application/MovementIdempotencyService.java
- [X] T012 Update account and transfer endpoint access rules in backend/src/main/java/com/example/banking/auth/config/SecurityConfig.java
- [X] T013 Create shared money/pagination/error DTO primitives in backend/src/main/java/com/example/banking/account/api/dto/

**Checkpoint**: Foundation complete; user stories can now be implemented.

---

## Phase 3: User Story 1 - Open and View Accounts (Priority: P1) 🎯 MVP

**Goal**: Let authenticated customers create checking/savings accounts and retrieve/list owned accounts.

**Independent Test**: Create one checking and one savings account, retrieve each by ID, and list all owned accounts from authenticated context.

### Tests for User Story 1

- [X] T014 [P] [US1] Add contract tests for create/list accounts in backend/src/test/java/com/example/banking/account/contract/AccountCreateListContractTest.java
- [X] T015 [P] [US1] Add contract test for get account details in backend/src/test/java/com/example/banking/account/contract/AccountGetContractTest.java
- [X] T016 [P] [US1] Add integration test for create-retrieve-list flow in backend/src/test/java/com/example/banking/account/integration/AccountCreateRetrieveListIntegrationTest.java

### Implementation for User Story 1

- [X] T017 [P] [US1] Implement create/list/get account request-response DTOs in backend/src/main/java/com/example/banking/account/api/dto/
- [X] T018 [US1] Implement create account application service in backend/src/main/java/com/example/banking/account/application/CreateAccountService.java
- [X] T019 [US1] Implement list accounts application service in backend/src/main/java/com/example/banking/account/application/ListAccountsService.java
- [X] T020 [US1] Implement get account application service in backend/src/main/java/com/example/banking/account/application/GetAccountService.java
- [X] T021 [US1] Implement account create/list/get endpoints in backend/src/main/java/com/example/banking/account/api/AccountController.java
- [X] T022 [P] [US1] Implement frontend account create/get/list API methods in frontend/src/features/account/api/accounts.ts
- [X] T023 [US1] Implement authenticated account creation and listing UI in frontend/src/features/account/pages/AccountsPage.tsx
- [X] T024 [US1] Register account workspace route and nav link in frontend/src/app/routes.tsx and frontend/src/app/AppShell.tsx

**Checkpoint**: User Story 1 works independently as MVP.

---

## Phase 4: User Story 2 - Maintain Account Profile (Priority: P1)

**Goal**: Allow authenticated customers to update mutable account fields and delete eligible accounts with closeout handling.

**Independent Test**: Update nickname on owned account, reject immutable field updates, and delete eligible account while enforcing pending-movement rule.

### Tests for User Story 2

- [X] T025 [P] [US2] Add contract test for account update endpoint in backend/src/test/java/com/example/banking/account/contract/AccountUpdateContractTest.java
- [X] T026 [P] [US2] Add contract test for account delete and closeout rules in backend/src/test/java/com/example/banking/account/contract/AccountDeleteContractTest.java
- [X] T027 [P] [US2] Add integration test for update and delete eligibility workflow in backend/src/test/java/com/example/banking/account/integration/AccountUpdateDeleteIntegrationTest.java

### Implementation for User Story 2

- [X] T028 [P] [US2] Implement update/delete DTOs including closeoutDestinationAccountId in backend/src/main/java/com/example/banking/account/api/dto/
- [X] T029 [US2] Implement mutable-field update service in backend/src/main/java/com/example/banking/account/application/UpdateAccountService.java
- [X] T030 [US2] Implement delete eligibility and closeout workflow service in backend/src/main/java/com/example/banking/account/application/DeleteAccountWorkflowService.java
- [X] T031 [US2] Extend account controller with PATCH and DELETE handlers in backend/src/main/java/com/example/banking/account/api/AccountController.java
- [X] T032 [P] [US2] Add frontend update/delete account API methods in frontend/src/features/account/api/accounts.ts
- [X] T033 [US2] Add account update and delete-with-closeout UI actions in frontend/src/features/account/pages/AccountsPage.tsx

**Checkpoint**: User Story 2 works independently.

---

## Phase 5: User Story 3 - Deposit and Withdraw Funds (Priority: P1)

**Goal**: Support authenticated deposits and withdrawals with precision, idempotency, and insufficient-funds protection.

**Independent Test**: Post deposit and withdrawal on owned account, verify balances and movement records, and reject insufficient/invalid withdrawals.

### Tests for User Story 3

- [X] T034 [P] [US3] Add contract test for deposit endpoint in backend/src/test/java/com/example/banking/account/contract/DepositContractTest.java
- [X] T035 [P] [US3] Add contract test for withdrawal endpoint in backend/src/test/java/com/example/banking/account/contract/WithdrawalContractTest.java
- [X] T036 [P] [US3] Add integration test for deposit-withdraw flow and insufficient funds in backend/src/test/java/com/example/banking/account/integration/DepositWithdrawIntegrationTest.java

### Implementation for User Story 3

- [X] T037 [P] [US3] Implement deposit/withdraw request and response DTO mapping in backend/src/main/java/com/example/banking/account/api/dto/
- [X] T038 [US3] Implement movement posting service for deposit and withdrawal in backend/src/main/java/com/example/banking/account/application/AccountMovementService.java
- [X] T039 [US3] Implement deposit and withdrawal endpoints in backend/src/main/java/com/example/banking/account/api/AccountMovementController.java
- [X] T040 [P] [US3] Add frontend deposit/withdraw API methods in frontend/src/features/account/api/movements.ts
- [X] T041 [US3] Add deposit and withdrawal forms to account workspace in frontend/src/features/account/pages/AccountsPage.tsx
- [X] T042 [US3] Add frontend movement error-code mapping in frontend/src/features/account/api/errorMapper.ts

**Checkpoint**: User Story 3 works independently.

---

## Phase 6: User Story 4 - Transfer Funds (Priority: P1)

**Goal**: Support atomic transfers between eligible accounts with paired movement records and idempotent retries.

**Independent Test**: Execute transfer between two owned accounts, verify paired debit/credit records, and reject invalid pairing/insufficient funds.

### Tests for User Story 4

- [X] T043 [P] [US4] Add contract test for transfer endpoint in backend/src/test/java/com/example/banking/account/contract/TransferContractTest.java
- [X] T044 [P] [US4] Add integration test for atomic transfer and insufficient funds in backend/src/test/java/com/example/banking/account/integration/TransferIntegrationTest.java

### Implementation for User Story 4

- [X] T045 [P] [US4] Implement transfer request and response DTOs in backend/src/main/java/com/example/banking/account/api/dto/
- [X] T046 [US4] Implement atomic transfer service with paired movements in backend/src/main/java/com/example/banking/account/application/TransferFundsService.java
- [X] T047 [US4] Implement transfer endpoint controller in backend/src/main/java/com/example/banking/account/api/TransferController.java
- [X] T048 [P] [US4] Add frontend transfer API method in frontend/src/features/account/api/movements.ts
- [X] T049 [US4] Add transfer form and account selector UI in frontend/src/features/account/pages/AccountsPage.tsx
- [X] T050 [US4] Add transfer idempotency-key handling in frontend/src/features/account/pages/AccountsPage.tsx

**Checkpoint**: User Story 4 works independently.

---

## Phase 7: User Story 5 - View Transaction History (Priority: P2)

**Goal**: Provide authenticated transaction history with stable ordering, filtering, and pagination.

**Independent Test**: Query history for owned account after movements, verify descending order/pagination, and reject invalid filter ranges.

### Tests for User Story 5

- [X] T051 [P] [US5] Add contract test for transaction history endpoint filters in backend/src/test/java/com/example/banking/account/contract/TransactionHistoryContractTest.java
- [X] T052 [P] [US5] Add integration test for history ordering and pagination in backend/src/test/java/com/example/banking/account/integration/TransactionHistoryIntegrationTest.java

### Implementation for User Story 5

- [X] T053 [P] [US5] Implement transaction history query DTOs in backend/src/main/java/com/example/banking/account/api/dto/
- [X] T054 [US5] Implement transaction history service with filter validation in backend/src/main/java/com/example/banking/account/application/GetTransactionHistoryService.java
- [X] T055 [US5] Extend movement controller with history endpoint in backend/src/main/java/com/example/banking/account/api/AccountMovementController.java
- [X] T056 [P] [US5] Add frontend transaction history API method in frontend/src/features/account/api/movements.ts
- [X] T057 [US5] Add transaction history table with filter and pagination UI in frontend/src/features/account/pages/AccountsPage.tsx

**Checkpoint**: User Story 5 works independently.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Finish shared quality gates, documentation, and integration refinements across stories.

- [X] T058 [P] Regenerate frontend typed API client for account contract in frontend/src/api/generated/
- [X] T059 Update account contract test includes in Maven contract profile in backend/pom.xml
- [X] T060 [P] Add account operations entry points on authenticated home page in frontend/src/features/auth/pages/HomePage.tsx
- [X] T061 Implement redaction-safe account audit logging service in backend/src/main/java/com/example/banking/account/application/AccountAuditService.java
- [X] T062 [P] Add local validation command matrix for account stories in specs/003-bank-account-operations/quickstart.md
- [X] T063 Run end-to-end quickstart scenarios and record outcomes in specs/003-bank-account-operations/checklists/requirements.md
- [X] T064 [P] Document account endpoint-to-error-code traceability in specs/003-bank-account-operations/spec.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies; start immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all user-story work.
- **Phase 3-7 (User Stories)**: Depend on Phase 2 completion.
- **Phase 8 (Polish)**: Depends on completion of selected user stories.

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational phase.
- **US2 (P1)**: Depends on Foundational phase; reuses account CRUD surfaces from US1.
- **US3 (P1)**: Depends on Foundational phase; uses account entities from US1 paths.
- **US4 (P1)**: Depends on Foundational phase and movement primitives from US3.
- **US5 (P2)**: Depends on Foundational phase and posted movement data from US3/US4.

### Within Each User Story

- Contract/integration tests are authored first and must fail before implementation.
- DTOs/entities precede services.
- Services precede controllers and frontend integration.
- Story implementation completes before moving to the next dependent story.

### Parallel Opportunities

- Setup tasks marked [P] can run concurrently.
- Foundational tasks marked [P] can run concurrently once migration sequencing is respected.
- In each user story, [P] test tasks can run in parallel.
- Frontend API integration tasks marked [P] can run in parallel with backend controller work after service contracts stabilize.

---

## Parallel Execution Examples

### User Story 1

- Run in parallel: T014 and T015 and T016
- Run in parallel: T017 and T022

### User Story 3

- Run in parallel: T034 and T035 and T036
- Run in parallel: T037 and T040

### User Story 5

- Run in parallel: T051 and T052
- Run in parallel: T053 and T056

---

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (Foundational).
3. Complete Phase 3 (US1).
4. Validate US1 independently before expanding scope.

### Incremental Delivery

1. Deliver US1 (account open/view) as MVP.
2. Add US2 (update/delete) and validate independently.
3. Add US3 (deposit/withdraw) and validate independently.
4. Add US4 (transfer) and validate independently.
5. Add US5 (history) and validate independently.
6. Complete Phase 8 polish and local verification.

### Parallel Team Strategy

1. Team completes Setup + Foundational together.
2. After Foundational: one stream on US1/US2, one stream on US3, one stream on US4/US5 as dependencies clear.
3. Merge only after each story’s independent tests pass.

---

## Notes

- [P] tasks indicate independent files and no unresolved prerequisite on incomplete tasks.
- [USx] labels ensure direct traceability from tasks to user stories.
- Every task line follows required checklist format with explicit file paths.
