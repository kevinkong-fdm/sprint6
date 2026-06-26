# Data Model: Customer Profile Lifecycle Management

All entities below are persisted in MySQL 8.4.

## Entity: CustomerProfile

- Purpose: Canonical customer identity and mutable profile details.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| customer_id | UUID (char/varchar) | Primary key, immutable | System-generated customer identifier |
| email | String | Required, format=email, max 254 | Source email as provided |
| email_normalized | String | Required, unique | Lowercase/trimmed email used for uniqueness checks |
| given_name | String | Required, max 100 | Mutable profile field |
| family_name | String | Required, max 100 | Mutable profile field |
| phone_number | String (nullable) | Optional, max 30 | Mutable profile field |
| date_of_birth | Date (nullable) | Optional | Immutable after creation by policy (if configured) |
| preferred_language | String (nullable) | Optional, max 20 | Mutable profile field |
| created_at | Timestamp | Required, immutable | Record creation timestamp |
| created_by | String | Required, immutable | Authenticated operator identifier |
| updated_at | Timestamp | Required | Last accepted update timestamp |
| updated_by | String | Required | Operator who submitted last accepted update |

Validation rules:
- `email_normalized` must be unique.
- Immutable fields cannot be changed by update operations.
- Failed updates produce no partial mutations.

## Entity: CustomerAddress

- Purpose: Dependent postal address records for a customer profile.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| address_id | UUID (char/varchar) | Primary key | Address record identifier |
| customer_id | UUID | Required, FK -> CustomerProfile.customer_id | Owning customer |
| address_type | Enum(HOME, MAILING, WORK) | Required | Address usage category |
| line1 | String | Required, max 200 | Mutable |
| line2 | String (nullable) | Optional, max 200 | Mutable |
| city | String | Required, max 100 | Mutable |
| region | String (nullable) | Optional, max 100 | Mutable |
| postal_code | String | Required, max 20 | Mutable |
| country_code | String | Required, ISO-3166 alpha-2 | Mutable |
| created_at | Timestamp | Required | Audit timestamp |
| updated_at | Timestamp | Required | Audit timestamp |

Validation rules:
- Address records are cascade-deleted when owning customer is hard-deleted.

## Entity: CustomerContactPreference

- Purpose: Customer communication preferences tied to the profile.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| preference_id | UUID (char/varchar) | Primary key | Preference record identifier |
| customer_id | UUID | Required, FK -> CustomerProfile.customer_id | Owning customer |
| channel | Enum(EMAIL, SMS, PUSH) | Required | Contact channel |
| opt_in | Boolean | Required | Consent preference |
| updated_at | Timestamp | Required | Last preference update |
| updated_by | String | Required | Operator identity |

Validation rules:
- Preference records are cascade-deleted when owning customer is hard-deleted.

## Entity: CustomerLifecycleEvent

- Purpose: Auditable lifecycle events for create/read/update/delete operations.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| event_id | UUID (char/varchar) | Primary key | Event record identifier |
| customer_id | UUID (nullable) | Optional | Null only when customer not found |
| action | Enum(CREATE, GET, UPDATE, DELETE) | Required | Lifecycle action type |
| outcome | Enum(SUCCESS, FAILURE) | Required | Action result |
| error_code | String (nullable) | Optional | Contract-aligned failure code |
| actor_id | String | Required | Authenticated operator identity |
| correlation_id | String | Required | Request trace identifier |
| occurred_at | Timestamp | Required | Event timestamp |
| metadata_json | JSON | Required | Sanitized contextual metadata |

Validation rules:
- Sensitive personal data must be redacted from event metadata.
- Delete actions must include cascade deletion outcome context.

## Relationships

- CustomerProfile 1..* CustomerAddress
- CustomerProfile 1..* CustomerContactPreference
- CustomerProfile 1..* CustomerLifecycleEvent

## Hard Delete and Cascading Rules

- Deleting CustomerProfile triggers cascade deletion of CustomerAddress and CustomerContactPreference in the same transaction.
- If any cascading dependency delete fails, the entire delete operation rolls back.
- After successful hard delete, subsequent get/update/delete operations for the same customer_id return customer-not-found behavior.

## Concurrency Behavior

- Concurrent valid profile updates follow last-write-wins semantics.
- `updated_at` and `updated_by` reflect the most recently accepted update.
- Validation failures remain atomic and do not create partial writes.
