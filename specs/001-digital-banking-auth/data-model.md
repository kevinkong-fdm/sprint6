# Data Model: Digital Banking Access and Session Management

All entities below are persisted in MySQL 8.x.

## Entity: UserAccount

- Purpose: Registered customer identity and account lifecycle.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID (char/varchar) | Primary key | Immutable account identifier |
| email | String | Required, format=email, max 254 | User-provided email |
| email_normalized | String | Required, unique, lower-case | Used for uniqueness/auth lookup |
| password_hash | String | Required | Argon2id hash output only |
| status | Enum(ACTIVE, LOCKED, SUSPENDED) | Required | Account lifecycle state |
| failed_login_attempts | Integer | Required, min 0 | Consecutive failed attempts counter |
| lockout_until | Timestamp (nullable) | Null or future timestamp | Set when lockout triggered |
| created_at | Timestamp | Required | Creation audit time |
| updated_at | Timestamp | Required | Last update time |

Validation rules:
- `email_normalized` is unique and compared case-insensitively.
- Lockout is triggered at 10 consecutive failed attempts and lasts 15 minutes.

## Entity: RefreshTokenSession

- Purpose: Persist refresh-token lineage for rotation and replay detection.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID (char/varchar) | Primary key | Token record ID |
| user_id | UUID | Required, FK -> UserAccount.id | Token owner |
| token_hash | String | Required, unique | Never store raw token |
| family_id | UUID | Required | Rotation lineage grouping |
| previous_token_id | UUID (nullable) | FK self-reference | Previous token in chain |
| issued_at | Timestamp | Required | Issuance time |
| expires_at | Timestamp | Required | Issued + 30 days |
| revoked_at | Timestamp (nullable) | Null or timestamp | Revoked/terminated marker |
| revocation_reason | String (nullable) | Optional | e.g., logout, reuse-detected |
| created_ip | String (nullable) | Optional | Source IP metadata |
| created_user_agent | String (nullable) | Optional | Client metadata |

Validation rules:
- Refresh token validity window is 30 days.
- Reuse of rotated token marks token family compromised and requires re-authentication.

## Entity: PasswordResetRequest

- Purpose: Single-use password reset request lifecycle.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID (char/varchar) | Primary key | Reset request ID |
| user_id | UUID | Required, FK -> UserAccount.id | Account owner |
| token_hash | String | Required, unique | Never store raw reset token |
| requested_at | Timestamp | Required | Request time |
| expires_at | Timestamp | Required | Requested + 30 minutes |
| consumed_at | Timestamp (nullable) | Null or timestamp | Marks single-use consumption |
| request_ip | String (nullable) | Optional | Abuse/audit metadata |
| request_user_agent | String (nullable) | Optional | Abuse/audit metadata |

Validation rules:
- Maximum 5 reset requests per account email in rolling 1-hour window.
- Links expire after 30 minutes and cannot be reused.

## Entity: LoginAttemptCounter

- Purpose: Persist per-account failed-login counters and lockout windows in MySQL.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| account_email_normalized | String | Primary key | Counter identity |
| failed_count | Integer | Required, min 0 | Consecutive failed attempts |
| window_started_at | Timestamp | Required | Counter window anchor |
| lockout_until | Timestamp (nullable) | Null or timestamp | 15-minute lockout end |
| updated_at | Timestamp | Required | Last mutation time |

Validation rules:
- Counter and lockout mutations must be transaction-safe for concurrent requests.

## Entity: PasswordResetThrottleCounter

- Purpose: Persist per-email reset request count windows in MySQL.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| account_email_normalized | String | Primary key | Counter identity |
| request_count | Integer | Required, min 0 | Requests in current rolling hour |
| window_started_at | Timestamp | Required | Counter window anchor |
| updated_at | Timestamp | Required | Last mutation time |

Validation rules:
- Sixth request inside the same rolling hour is rejected with reset-throttled behavior.

## Entity: AuthenticationEvent

- Purpose: Auditable security event stream.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | UUID (char/varchar) | Primary key | Event ID |
| user_id | UUID (nullable) | FK -> UserAccount.id | Null for unknown-email attempts |
| email_fingerprint | String (nullable) | Optional | Redacted/hashed representation |
| event_type | Enum | Required | REGISTER, LOGIN_FAIL, LOGIN_LOCKOUT, RESET_REQUEST, TOKEN_REFRESH, TOKEN_REUSE, etc. |
| outcome | Enum(SUCCESS, FAILURE) | Required | Result state |
| error_code | String (nullable) | Optional | Spec-aligned code |
| correlation_id | String | Required | Traceability across services |
| occurred_at | Timestamp | Required | Event time |
| metadata_json | JSON | Required | Sanitized context only |

Validation rules:
- Metadata must not include plaintext passwords, tokens, or secrets.

## Relationships

- UserAccount 1..* RefreshTokenSession
- UserAccount 1..* PasswordResetRequest
- UserAccount 1..* AuthenticationEvent
- UserAccount 1..1 LoginAttemptCounter (by normalized email)
- UserAccount 1..1 PasswordResetThrottleCounter (by normalized email)
- RefreshTokenSession self-reference via `previous_token_id` for rotation lineage

## State Transitions

### UserAccount
- ACTIVE -> LOCKED: after 10 consecutive failed login attempts
- LOCKED -> ACTIVE: after 15-minute lockout expiry (or administrative unlock)
- ACTIVE/LOCKED -> SUSPENDED: administrative/security action

### RefreshTokenSession
- ACTIVE -> ROTATED: when used successfully for refresh and replaced
- ACTIVE/ROTATED -> REVOKED: logout, compromise handling, or family invalidation
- Any token with `expires_at` in the past -> EXPIRED

### PasswordResetRequest
- ISSUED -> CONSUMED: successful use before expiry
- ISSUED -> EXPIRED: after 30 minutes
- Only one of `CONSUMED` or `EXPIRED` may apply for final state
