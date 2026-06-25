# Authentication Overview

This document describes the Spring Boot + React + MySQL authentication foundation.

## Endpoints

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/password-reset/request`
- `POST /auth/token/refresh`

## Policy Values

- Login identifier: email only
- MFA: not supported in v1
- Access token lifetime: 60 minutes
- Refresh token lifetime: 30 days
- Login lockout: 10 failed attempts -> 15 minutes
- Password reset throttle: 5 requests/hour per normalized email
- Password reset link expiry: 30 minutes

## Frontend Integration Rules

- Use `frontend/src/api/client.ts` for all auth API calls.
- Use `frontend/src/features/auth/api/errorMapper.ts` for error translation.
- Avoid direct ad-hoc `fetch` calls from page components.

## Backend Integration Rules

- Controllers delegate to services and avoid business logic.
- Domain exceptions are mapped by `GlobalExceptionHandler`.
- Correlation ID is supplied by `CorrelationIdFilter`.
- Entity persistence is handled via Spring Data repositories.
