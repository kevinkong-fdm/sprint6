/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AcceptedResponse } from '../models/AcceptedResponse';
import type { AuthTokenResponse } from '../models/AuthTokenResponse';
import type { LoginRequest } from '../models/LoginRequest';
import type { PasswordResetRequest } from '../models/PasswordResetRequest';
import type { RegisterRequest } from '../models/RegisterRequest';
import type { RegisterResponse } from '../models/RegisterResponse';
import type { TokenRefreshRequest } from '../models/TokenRefreshRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AuthenticationService {
    /**
     * Register customer account
     * @returns RegisterResponse Account created
     * @throws ApiError
     */
    public static registerCustomer({
        requestBody,
    }: {
        requestBody: RegisterRequest,
    }): CancelablePromise<RegisterResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/auth/register',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Registration validation failure`,
                409: `Duplicate account email`,
            },
        });
    }
    /**
     * Authenticate customer
     * Authenticates with email and password only.
     * MFA challenge fields are not supported in v1.
     *
     * @returns AuthTokenResponse Authentication successful
     * @throws ApiError
     */
    public static loginCustomer({
        requestBody,
    }: {
        requestBody: LoginRequest,
    }): CancelablePromise<AuthTokenResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/auth/login',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Invalid credentials`,
                423: `Account locked after failed attempts`,
            },
        });
    }
    /**
     * Request password reset
     * Response semantics are non-disclosing for account existence.
     * A successful outward response does not confirm whether the email exists.
     *
     * @returns AcceptedResponse Request accepted (neutral response)
     * @throws ApiError
     */
    public static requestPasswordReset({
        requestBody,
    }: {
        requestBody: PasswordResetRequest,
    }): CancelablePromise<AcceptedResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/auth/password-reset/request',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                429: `Reset request throttled`,
            },
        });
    }
    /**
     * Refresh session tokens
     * @returns AuthTokenResponse New token set issued and prior refresh token invalidated
     * @throws ApiError
     */
    public static refreshSessionToken({
        requestBody,
    }: {
        requestBody: TokenRefreshRequest,
    }): CancelablePromise<AuthTokenResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/auth/token/refresh',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Invalid, expired, revoked, or reused refresh token`,
            },
        });
    }
}
