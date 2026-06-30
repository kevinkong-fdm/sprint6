# Tasks: Authenticated Standing Orders, Notifications, Statements, and Spending Insights

**Input**: Design documents from `/specs/004-standing-orders-insights/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/standing-orders-insights.openapi.yaml, quickstart.md

**Tests**: Contract and integration tests are REQUIRED for this feature per plan/research quality gates.

**Organization**: Tasks are grouped by user story so each story can be implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize feature scaffolding, route placeholders, and contract generation hooks.

- [X] T001 Update standing-orders OpenAPI generation script in frontend/package.json
- [X] T002 Create standing-order domain package marker in backend/src/main/java/com/example/banking/standingorder/package-info.java
- [X] T003 [P] Create notification domain package marker in backend/src/main/java/com/example/banking/notification/package-info.java
- [X] T004 [P] Create statement domain package marker in backend/src/main/java/com/example/banking/statement/package-info.java
- [X] T005 [P] Create insights domain package marker in backend/src/main/java/com/example/banking/insights/package-info.java
- [X] T006 [P] Create standing-orders contract test package marker in backend/src/test/java/com/example/banking/standingorder/contract/package-info.java
- [X] T007 [P] Create standing-orders integration test package marker in backend/src/test/java/com/example/banking/standingorder/integration/package-info.java
- [X] T008 [P] Create notifications contract test package marker in backend/src/test/java/com/example/banking/notification/contract/package-info.java
- [X] T009 [P] Create notifications integration test package marker in backend/src/test/java/com/example/banking/notification/integration/package-info.java
- [X] T010 [P] Create statements contract test package marker in backend/src/test/java/com/example/banking/statement/contract/package-info.java
- [X] T011 [P] Create statements integration test package marker in backend/src/test/java/com/example/banking/statement/integration/package-info.java
- [X] T012 [P] Create insights contract test package marker in backend/src/test/java/com/example/banking/insights/contract/package-info.java
- [X] T013 [P] Create insights integration test package marker in backend/src/test/java/com/example/banking/insights/integration/package-info.java
- [X] T014 [P] Create frontend standing-orders feature folders in frontend/src/features/standing-orders/
- [X] T015 [P] Create frontend notifications feature folders in frontend/src/features/notifications/
- [X] T016 [P] Create frontend statements feature folders in frontend/src/features/statements/
- [X] T017 [P] Create frontend insights feature folders in frontend/src/features/insights/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build shared schema, security, error handling, and primitives required before any user story work.

**CRITICAL**: No user story implementation starts before this phase is complete.

- [X] T018 Create Flyway migration for standing-orders, execution, notifications, statements, and insights tables in backend/src/main/resources/db/migration/V5__standing_orders_insights_baseline.sql
- [X] T019 [P] Implement standing-order core enums/entities in backend/src/main/java/com/example/banking/standingorder/domain/
- [X] T020 [P] Implement notification core enums/entities in backend/src/main/java/com/example/banking/notification/domain/
- [X] T021 [P] Implement statement core entities in backend/src/main/java/com/example/banking/statement/domain/
- [X] T022 [P] Implement insights core entities in backend/src/main/java/com/example/banking/insights/domain/
- [X] T023 [P] Implement standing-order repositories in backend/src/main/java/com/example/banking/standingorder/infrastructure/
- [X] T024 [P] Implement notification repositories in backend/src/main/java/com/example/banking/notification/infrastructure/
- [X] T025 [P] Implement statement repositories in backend/src/main/java/com/example/banking/statement/infrastructure/
- [X] T026 [P] Implement insights repositories in backend/src/main/java/com/example/banking/insights/infrastructure/
- [X] T027 Implement feature error-code exception hierarchy in backend/src/main/java/com/example/banking/standingorder/application/StandingOrderDomainException.java
- [X] T028 Implement feature exception handler and error envelope mapping in backend/src/main/java/com/example/banking/standingorder/api/StandingOrderExceptionHandler.java
- [X] T029 Implement authenticated ownership resolver for source and destination account checks in backend/src/main/java/com/example/banking/standingorder/application/StandingOrderAuthorizationService.java
- [X] T030 [P] Implement timezone utility for fixed AEST schedule and month-boundary calculations in backend/src/main/java/com/example/banking/standingorder/application/PlatformTimezoneService.java
- [X] T031 [P] Implement idempotency service for standing-order create and trigger paths in backend/src/main/java/com/example/banking/standingorder/application/StandingOrderIdempotencyService.java
- [X] T032 [P] Implement reusable notification dispatch retry service in backend/src/main/java/com/example/banking/notification/application/NotificationDispatchService.java
- [X] T033 [P] Implement reusable statement aggregation helper in backend/src/main/java/com/example/banking/statement/application/StatementAggregationService.java
- [X] T034 [P] Implement reusable insights aggregation helper in backend/src/main/java/com/example/banking/insights/application/InsightsAggregationService.java
- [X] T035 Update authenticated route access rules for new feature endpoints in backend/src/main/java/com/example/banking/auth/config/SecurityConfig.java
- [X] T036 [P] Add frontend generated API export wiring for new feature contract in frontend/src/api/generated/index.ts

**Checkpoint**: Foundation complete; user stories can now be implemented.

---

## Phase 3: User Story 1 - Configure Standing Orders (Priority: P1) 🎯 MVP

**Goal**: Let authenticated customers create, update, pause, resume, and cancel standing orders with internal same-customer destination enforcement.

**Independent Test**: Create a standing order, update mutable fields, pause/resume/cancel it, and verify destination authorization and external-destination rejection paths.

### Tests for User Story 1

- [ ] T037 [P] [US1] Add contract tests for create/list/get standing orders in backend/src/test/java/com/example/banking/standingorder/contract/StandingOrderCreateListGetContractTest.java
- [ ] T038 [P] [US1] Add contract tests for update and lifecycle transitions in backend/src/test/java/com/example/banking/standingorder/contract/StandingOrderUpdateLifecycleContractTest.java
- [ ] T039 [P] [US1] Add contract tests for destination-validation failures in backend/src/test/java/com/example/banking/standingorder/contract/StandingOrderDestinationValidationContractTest.java
- [ ] T040 [P] [US1] Add integration test for create-update-pause-resume-cancel flow in backend/src/test/java/com/example/banking/standingorder/integration/StandingOrderLifecycleIntegrationTest.java

### Implementation for User Story 1

- [X] T041 [P] [US1] Implement standing-order request/response DTOs in backend/src/main/java/com/example/banking/standingorder/api/dto/
- [X] T042 [US1] Implement create standing-order service with schedule validation in backend/src/main/java/com/example/banking/standingorder/application/CreateStandingOrderService.java
- [X] T043 [US1] Implement list/get standing-order services with deterministic ordering in backend/src/main/java/com/example/banking/standingorder/application/ListStandingOrdersService.java and backend/src/main/java/com/example/banking/standingorder/application/GetStandingOrderService.java
- [X] T044 [US1] Implement update standing-order service with mutable-field policy enforcement in backend/src/main/java/com/example/banking/standingorder/application/UpdateStandingOrderService.java
- [X] T045 [US1] Implement lifecycle transition service for pause/resume/cancel in backend/src/main/java/com/example/banking/standingorder/application/StandingOrderLifecycleService.java
- [X] T046 [US1] Implement standing-order controller endpoints for create/list/get/update/pause/resume/cancel in backend/src/main/java/com/example/banking/standingorder/api/StandingOrderController.java
- [X] T047 [P] [US1] Implement frontend standing-order API client wrapper in frontend/src/features/standing-orders/api/standingOrders.ts
- [X] T048 [US1] Implement standing-order management page for create/list/update/lifecycle actions in frontend/src/features/standing-orders/pages/StandingOrdersPage.tsx
- [X] T049 [US1] Register standing-orders route and navigation link in frontend/src/app/routes.tsx and frontend/src/app/AppShell.tsx
- [X] T050 [US1] Add frontend standing-order error mapping for SO-SET/SO-UPD/SO-DEL codes in frontend/src/features/standing-orders/api/errorMapper.ts

**Checkpoint**: User Story 1 works independently as MVP.

---

## Phase 4: User Story 2 - Receive Triggered Notifications (Priority: P1)

**Goal**: Record and retrieve standing-order lifecycle/execution notifications with deterministic dispatch status, while rejecting preference updates in this version.

**Independent Test**: Trigger success/failure outcomes, verify notification records and ordering, and confirm preference update endpoint rejects with NOTIFY-001.

### Tests for User Story 2

- [ ] T051 [P] [US2] Add contract tests for standing-order notifications listing in backend/src/test/java/com/example/banking/notification/contract/StandingOrderNotificationListContractTest.java
- [ ] T052 [P] [US2] Add contract test for unsupported preference update endpoint in backend/src/test/java/com/example/banking/notification/contract/NotificationPreferenceUpdateUnsupportedContractTest.java
- [ ] T053 [P] [US2] Add integration test for notification event generation and dispatch status transitions in backend/src/test/java/com/example/banking/notification/integration/NotificationDispatchIntegrationTest.java

### Implementation for User Story 2

- [X] T054 [P] [US2] Implement notification request/response DTOs in backend/src/main/java/com/example/banking/notification/api/dto/
- [X] T055 [US2] Implement notification event generation service for lifecycle and execution outcomes in backend/src/main/java/com/example/banking/notification/application/StandingOrderNotificationService.java
- [X] T056 [US2] Implement notification list query service with deterministic pagination in backend/src/main/java/com/example/banking/notification/application/ListNotificationsService.java
- [X] T057 [US2] Implement notification controller endpoints for list and unsupported preference update in backend/src/main/java/com/example/banking/notification/api/NotificationController.java
- [X] T058 [US2] Integrate standing-order lifecycle/execution services with notification event publishing in backend/src/main/java/com/example/banking/standingorder/application/
- [X] T059 [P] [US2] Implement frontend notifications API wrapper in frontend/src/features/notifications/api/notifications.ts
- [X] T060 [US2] Implement frontend notifications page with pagination and status filters in frontend/src/features/notifications/pages/NotificationsPage.tsx
- [X] T061 [US2] Register notifications route and navigation link in frontend/src/app/routes.tsx and frontend/src/app/AppShell.tsx
- [X] T062 [US2] Add frontend notifications error mapping for NOTIFY and AUTH-FEAT codes in frontend/src/features/notifications/api/errorMapper.ts

**Checkpoint**: User Story 2 works independently.

---

## Phase 5: User Story 3 - Generate Monthly Statements (Priority: P2)

**Goal**: Generate and retrieve month-scoped statements for authorized accounts with fixed AEST boundaries, including valid empty-activity months.

**Independent Test**: Generate/retrieve statement for valid closed month, verify totals and line items, validate empty-activity success shape, and reject invalid/future periods.

### Tests for User Story 3

- [ ] T063 [P] [US3] Add contract tests for monthly statement generation and retrieval in backend/src/test/java/com/example/banking/statement/contract/MonthlyStatementContractTest.java
- [ ] T064 [P] [US3] Add contract tests for statement validation and authorization failures in backend/src/test/java/com/example/banking/statement/contract/MonthlyStatementValidationContractTest.java
- [ ] T065 [P] [US3] Add integration test for statement month-boundary attribution in AEST in backend/src/test/java/com/example/banking/statement/integration/MonthlyStatementTimezoneIntegrationTest.java

### Implementation for User Story 3

- [X] T066 [P] [US3] Implement statement request/response DTOs in backend/src/main/java/com/example/banking/statement/api/dto/
- [X] T067 [US3] Implement monthly statement generation service with empty-activity behavior in backend/src/main/java/com/example/banking/statement/application/GenerateMonthlyStatementService.java
- [X] T068 [US3] Implement monthly statement retrieval service in backend/src/main/java/com/example/banking/statement/application/GetMonthlyStatementService.java
- [X] T069 [US3] Implement statement controller endpoints for generate and retrieve in backend/src/main/java/com/example/banking/statement/api/StatementController.java
- [X] T070 [US3] Integrate statement aggregation with account movements in backend/src/main/java/com/example/banking/statement/application/StatementAggregationService.java
- [X] T071 [P] [US3] Implement frontend statements API wrapper in frontend/src/features/statements/api/statements.ts
- [X] T072 [US3] Implement frontend statements page for generation/retrieval and line-item display in frontend/src/features/statements/pages/StatementsPage.tsx
- [X] T073 [US3] Register statements route and navigation link in frontend/src/app/routes.tsx and frontend/src/app/AppShell.tsx
- [X] T074 [US3] Add frontend statements error mapping for STMT and AUTH-FEAT codes in frontend/src/features/statements/api/errorMapper.ts

**Checkpoint**: User Story 3 works independently.

---

## Phase 6: User Story 4 - View Spending Insights (Priority: P2)

**Goal**: Provide categorized spending insights with period-over-period comparison and deterministic low-data success responses.

**Independent Test**: Request insights for valid ranges with and without sufficient history, verify category totals and deltas, and validate low-data success payload with insufficiency indicator.

### Tests for User Story 4

- [ ] T075 [P] [US4] Add contract tests for spending insights success and low-data responses in backend/src/test/java/com/example/banking/insights/contract/SpendingInsightsContractTest.java
- [ ] T076 [P] [US4] Add contract tests for invalid filter windows in backend/src/test/java/com/example/banking/insights/contract/SpendingInsightsValidationContractTest.java
- [ ] T077 [P] [US4] Add integration test for comparison delta calculation and insufficiency behavior in backend/src/test/java/com/example/banking/insights/integration/SpendingInsightsIntegrationTest.java

### Implementation for User Story 4

- [X] T078 [P] [US4] Implement insights request/response DTOs in backend/src/main/java/com/example/banking/insights/api/dto/
- [X] T079 [US4] Implement spending insights service with low-data success semantics in backend/src/main/java/com/example/banking/insights/application/GetSpendingInsightsService.java
- [X] T080 [US4] Implement insights controller endpoint and filter validation in backend/src/main/java/com/example/banking/insights/api/InsightsController.java
- [X] T081 [US4] Implement period comparison and category aggregation logic in backend/src/main/java/com/example/banking/insights/application/InsightsAggregationService.java
- [X] T082 [P] [US4] Implement frontend insights API wrapper in frontend/src/features/insights/api/insights.ts
- [X] T083 [US4] Implement frontend insights page with category and delta rendering plus low-data indicator in frontend/src/features/insights/pages/InsightsPage.tsx
- [X] T084 [US4] Register insights route and navigation link in frontend/src/app/routes.tsx and frontend/src/app/AppShell.tsx
- [X] T085 [US4] Add frontend insights error mapping for INS and AUTH-FEAT codes in frontend/src/features/insights/api/errorMapper.ts

**Checkpoint**: User Story 4 works independently.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Finish contract generation, route hardening, documentation, and end-to-end validation across all stories.

- [X] T086 [P] Regenerate frontend typed API client for standing-orders-insights contract in frontend/src/api/generated/
- [X] T087 Update Maven contract test profile includes for standingorder/notification/statement/insights contract tests in backend/pom.xml
- [X] T088 [P] Add authenticated quick links from home page to new feature pages in frontend/src/features/auth/pages/HomePage.tsx
- [X] T089 Implement redaction-safe feature audit logging for standing-orders and insights flows in backend/src/main/java/com/example/banking/standingorder/application/StandingOrderAuditService.java and backend/src/main/java/com/example/banking/insights/application/InsightsAuditService.java
- [X] T090 [P] Finalize local validation command matrix alignment for all four stories in specs/004-standing-orders-insights/quickstart.md
- [X] T091 Execute local backend/frontend validation commands and record results in specs/004-standing-orders-insights/checklists/requirements.md
- [X] T092 [P] Add endpoint-to-error-code traceability appendix for implemented operations in specs/004-standing-orders-insights/spec.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies; start immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all user-story work.
- **Phase 3-6 (User Stories)**: Depend on Phase 2 completion.
- **Phase 7 (Polish)**: Depends on completion of selected user stories.

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational phase and is the MVP story.
- **US2 (P1)**: Depends on Foundational phase and integrates with standing-order lifecycle/execution events from US1.
- **US3 (P2)**: Depends on Foundational phase and posted movement data from existing account operations.
- **US4 (P2)**: Depends on Foundational phase and posted movement/category data surfaced by statement/insights aggregation.

### Within Each User Story

- Contract/integration tests are authored first and should fail before implementation.
- DTOs/entities precede services.
- Services precede controllers and frontend integration.
- Story implementation completes before moving to dependent stories.

### Parallel Opportunities

- Setup tasks marked [P] can run concurrently.
- Foundational tasks marked [P] can run concurrently once migration sequencing is respected.
- In each user story, [P] test tasks can run in parallel.
- Frontend API integration tasks marked [P] can run in parallel with backend controller work after service contracts stabilize.

---

## Parallel Execution Examples

### User Story 1

- Run in parallel: T037 and T038 and T039 and T040
- Run in parallel: T041 and T047 and T050

### User Story 2

- Run in parallel: T051 and T052 and T053
- Run in parallel: T054 and T059 and T062

### User Story 3

- Run in parallel: T063 and T064 and T065
- Run in parallel: T066 and T071 and T074

### User Story 4

- Run in parallel: T075 and T076 and T077
- Run in parallel: T078 and T082 and T085

---

## Implementation Strategy

### MVP First (User Story 1)

1. Complete Phase 1 (Setup).
2. Complete Phase 2 (Foundational).
3. Complete Phase 3 (US1).
4. Validate US1 independently before expanding scope.

### Incremental Delivery

1. Deliver US1 (standing-order setup/lifecycle) as MVP.
2. Add US2 (notifications) and validate independently.
3. Add US3 (monthly statements) and validate independently.
4. Add US4 (spending insights) and validate independently.
5. Complete Phase 7 polish and local verification.

### Parallel Team Strategy

1. Team completes Setup + Foundational together.
2. After Foundational: one stream on US1, one stream on US2, one stream on US3/US4 as dependencies clear.
3. Merge only after each story’s independent tests pass.

---

## Notes

- [P] tasks indicate independent files and no unresolved prerequisite on incomplete tasks.
- [USx] labels ensure direct traceability from tasks to user stories.
- Every task line follows required checklist format with explicit file paths.

