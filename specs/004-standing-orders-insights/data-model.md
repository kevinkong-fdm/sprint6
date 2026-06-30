# Data Model: Authenticated Standing Orders, Notifications, Statements, and Spending Insights

All persisted entities are stored in local MySQL 8.x for this feature increment.

## Entity: StandingOrder

- Purpose: Customer-managed recurring payment instruction between owned internal platform accounts.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| standing_order_id | UUID | Primary key, immutable | Public standing-order identifier |
| customer_id | UUID | Required, indexed | Authenticated owner identity |
| source_account_id | UUID | Required | Must belong to `customer_id` |
| destination_account_id | UUID | Required | Internal platform account; must belong to same `customer_id` |
| amount | DECIMAL(19,4) | Required, > 0 | Recurring transfer amount |
| frequency | ENUM(DAILY, WEEKLY, MONTHLY) | Required | Execution cadence |
| start_date | DATE | Required | Inclusive start date in platform timezone |
| end_date | DATE | Nullable | Optional inclusive end date |
| status | ENUM(ACTIVE, PAUSED, CANCELED) | Required | Lifecycle state |
| next_execution_at | TIMESTAMP | Nullable | Due timestamp in AEST semantics |
| last_execution_at | TIMESTAMP | Nullable | Most recent execution attempt |
| idempotency_key | VARCHAR(120) | Nullable, indexed | Setup idempotency token |
| created_at | TIMESTAMP | Required | Creation audit time |
| updated_at | TIMESTAMP | Required | Last mutation audit time |

Validation rules:
- `start_date` must be on or before `end_date` when `end_date` is present.
- `destination_account_id` must be an internal account owned by the same authenticated customer.
- `status` transitions must follow defined lifecycle rules.

## Entity: StandingOrderExecution

- Purpose: Immutable log of each execution attempt for a standing order.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| standing_order_execution_id | UUID | Primary key | Execution record identifier |
| standing_order_id | UUID | Required, FK -> StandingOrder | Parent standing order |
| scheduled_for | TIMESTAMP | Required | Planned due time in AEST semantics |
| triggered_at | TIMESTAMP | Required | Actual trigger time |
| outcome | ENUM(SUCCESS, FAILED, SKIPPED) | Required | Deterministic execution outcome |
| failure_reason_code | VARCHAR(32) | Nullable | `SO-EXE-001`/`SO-EXE-002` when applicable |
| transfer_reference_id | UUID | Nullable | Links resulting transfer event when posted |
| idempotency_key | VARCHAR(120) | Nullable, indexed | Trigger idempotency token |
| correlation_id | VARCHAR(64) | Required | Traceability identifier |
| created_at | TIMESTAMP | Required | Record creation time |

Validation rules:
- Exactly one execution outcome per standing-order due cycle.
- Failed and skipped outcomes must not create partial posted money movements.

## Entity: NotificationPreference

- Purpose: System-managed notification policy for standing-order events.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| customer_id | UUID | Primary key | One policy row per customer |
| standing_order_notifications_enabled | BOOLEAN | Required, default true | Fixed true in this version |
| managed_by_system | BOOLEAN | Required, default true | Prevents customer mutation |
| created_at | TIMESTAMP | Required | Creation audit time |
| updated_at | TIMESTAMP | Required | Last update audit time |

Validation rules:
- Customer-facing update attempts are rejected with `NOTIFY-001`.

## Entity: NotificationEvent

- Purpose: Customer-visible standing-order lifecycle and execution notifications.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| notification_event_id | UUID | Primary key | Notification identifier |
| customer_id | UUID | Required, indexed | Notification recipient |
| standing_order_id | UUID | Nullable | Related standing order |
| standing_order_execution_id | UUID | Nullable | Related execution attempt |
| event_type | ENUM(LIFECYCLE_UPDATED, EXECUTION_SUCCESS, EXECUTION_FAILURE, EXECUTION_SKIPPED) | Required | Notification category |
| title | VARCHAR(140) | Required | User-safe summary |
| message | VARCHAR(1000) | Required | User-safe notification body |
| dispatch_status | ENUM(PENDING, SENT, FAILED) | Required | Dispatch outcome status |
| dispatch_attempt_count | INTEGER | Required, min 0 | Retry traceability |
| correlation_id | VARCHAR(64) | Required | Traceability identifier |
| created_at | TIMESTAMP | Required | Event creation time |
| dispatched_at | TIMESTAMP | Nullable | Timestamp when marked SENT |

Validation rules:
- Duplicate visible notifications for the same execution outcome are not allowed.
- Failed dispatch after retry policy maps to `NOTIFY-002`.

## Entity: MonthlyStatement

- Purpose: Month-scoped account statement summary for authorized customers.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| monthly_statement_id | UUID | Primary key | Statement record identifier |
| account_id | UUID | Required, indexed | Account under statement |
| customer_id | UUID | Required, indexed | Authorized owner identity |
| statement_month | CHAR(7) | Required (`YYYY-MM`) | Calendar month in AEST semantics |
| opening_balance | DECIMAL(19,4) | Required | Balance at month start |
| closing_balance | DECIMAL(19,4) | Required | Balance at month end |
| total_debits | DECIMAL(19,4) | Required, >= 0 | Month debit total |
| total_credits | DECIMAL(19,4) | Required, >= 0 | Month credit total |
| line_item_count | INTEGER | Required, >= 0 | Included activity count |
| generated_at | TIMESTAMP | Required | Statement generation timestamp |

Validation rules:
- Future months and unsupported periods are rejected with `STMT-001`.
- Valid no-activity months still return a statement with zero totals.

## Entity: StatementLineItem

- Purpose: Transaction-level detail included in a monthly statement.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| statement_line_item_id | UUID | Primary key | Line-item identifier |
| monthly_statement_id | UUID | Required, FK -> MonthlyStatement | Parent statement |
| transaction_id | UUID | Required | Linked account movement identifier |
| posted_at | TIMESTAMP | Required | Posting time |
| entry_type | ENUM(DEBIT, CREDIT) | Required | Direction in statement |
| amount | DECIMAL(19,4) | Required, > 0 | Absolute movement amount |
| balance_after | DECIMAL(19,4) | Required | Balance after movement |
| description | VARCHAR(200) | Required | User-visible entry description |

Validation rules:
- Line items are ordered deterministically by posted timestamp then transaction ID.

## Entity: SpendingInsightSnapshot

- Purpose: Deterministic period-scoped spending insights and trend comparison outputs.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| insight_snapshot_id | UUID | Primary key | Snapshot identifier |
| customer_id | UUID | Required, indexed | Insight owner identity |
| period_start | DATE | Required | Selected period start |
| period_end | DATE | Required | Selected period end |
| comparison_mode | ENUM(PREVIOUS_PERIOD, NONE) | Required | Comparison window behavior |
| insufficient_data | BOOLEAN | Required | True when historical data is inadequate |
| generated_at | TIMESTAMP | Required | Snapshot generation time |
| correlation_id | VARCHAR(64) | Required | Traceability identifier |

Validation rules:
- Invalid or unsupported filter windows return `INS-001`.
- When `insufficient_data = true`, response still returns HTTP success and partial metrics.

## Entity: SpendingCategoryMetric

- Purpose: Category-level totals and trend deltas associated with one insight snapshot.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| spending_category_metric_id | UUID | Primary key | Metric identifier |
| insight_snapshot_id | UUID | Required, FK -> SpendingInsightSnapshot | Parent snapshot |
| category_code | VARCHAR(64) | Required | Spending category key |
| current_total | DECIMAL(19,4) | Required, >= 0 | Current period spend |
| previous_total | DECIMAL(19,4) | Nullable, >= 0 | Previous period spend when available |
| delta_amount | DECIMAL(19,4) | Nullable | Current minus previous |
| delta_percent | DECIMAL(9,4) | Nullable | Percent delta when calculable |

Validation rules:
- `delta_*` values can be null when insufficient history exists.

## Entity: StandardErrorEnvelope

- Purpose: Contract-level deterministic error payload for all feature operations.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| error_code | VARCHAR(32) | Required | Spec-defined deterministic code |
| message | VARCHAR(500) | Required | User-safe message |
| correlation_id | VARCHAR(64) | Required | Request trace identifier |
| timestamp | TIMESTAMP | Required | Error emission time |

## Relationships

- StandingOrder 1..* StandingOrderExecution
- StandingOrder 1..* NotificationEvent
- NotificationPreference 1..* NotificationEvent (by customer)
- MonthlyStatement 1..* StatementLineItem
- SpendingInsightSnapshot 1..* SpendingCategoryMetric
- Customer ownership (`customer_id`) anchors access control across all entities

## State Transitions

### StandingOrder

- `ACTIVE -> PAUSED`
- `PAUSED -> ACTIVE`
- `ACTIVE -> CANCELED`
- `PAUSED -> CANCELED`
- `CANCELED` is terminal

### StandingOrderExecution

- `TRIGGERED -> SUCCESS`
- `TRIGGERED -> FAILED`
- `TRIGGERED -> SKIPPED`
- Final outcome is immutable once set

### NotificationEvent

- `PENDING -> SENT`
- `PENDING -> FAILED`
- `FAILED -> SENT` (retry success)

## Invariants

- All in-scope operations require authenticated customer context and ownership checks.
- Standing-order execution and associated posting effects are atomic.
- Destination account ownership must match source account ownership for every standing-order create/update/execute path.
- Schedule due evaluation and statement month attribution always use fixed AEST (UTC+10:00).
- Low-data spending insights return successful responses with deterministic insufficiency signaling.
