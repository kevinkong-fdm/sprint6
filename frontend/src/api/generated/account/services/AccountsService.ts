/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { AccountListResponse } from '../models/AccountListResponse';
import type { AccountResponse } from '../models/AccountResponse';
import type { CreateAccountRequest } from '../models/CreateAccountRequest';
import type { DeleteAccountRequest } from '../models/DeleteAccountRequest';
import type { UpdateAccountRequest } from '../models/UpdateAccountRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AccountsService {
    /**
     * Create account
     * @returns AccountResponse Account created
     * @throws ApiError
     */
    public static createAccount({
        requestBody,
    }: {
        requestBody: CreateAccountRequest,
    }): CancelablePromise<AccountResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/accounts',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
            },
        });
    }
    /**
     * List customer accounts
     * @returns AccountListResponse Accounts listed
     * @throws ApiError
     */
    public static listAccounts({
        accountType,
        page = 1,
        size = 20,
    }: {
        accountType?: 'CHECKING' | 'SAVINGS',
        page?: number,
        size?: number,
    }): CancelablePromise<AccountListResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/accounts',
            query: {
                'accountType': accountType,
                'page': page,
                'size': size,
            },
            errors: {
                401: `Authentication required or invalid session`,
            },
        });
    }
    /**
     * Retrieve account details
     * @returns AccountResponse Account found
     * @throws ApiError
     */
    public static getAccount({
        accountId,
    }: {
        accountId: string,
    }): CancelablePromise<AccountResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/accounts/{accountId}',
            path: {
                'accountId': accountId,
            },
            errors: {
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                404: `Account not found`,
            },
        });
    }
    /**
     * Update account
     * @returns AccountResponse Account updated
     * @throws ApiError
     */
    public static updateAccount({
        accountId,
        requestBody,
    }: {
        accountId: string,
        requestBody: UpdateAccountRequest,
    }): CancelablePromise<AccountResponse> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/accounts/{accountId}',
            path: {
                'accountId': accountId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                404: `Account not found`,
                422: `Immutable field update attempted`,
            },
        });
    }
    /**
     * Delete account
     * Deletes the target account when no pending movement exists. If source balance is non-zero, closeoutDestinationAccountId is required and a closeout transfer is posted atomically before deletion.
     * @returns void
     * @throws ApiError
     */
    public static deleteAccount({
        accountId,
        requestBody,
    }: {
        accountId: string,
        requestBody?: DeleteAccountRequest,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'DELETE',
            url: '/accounts/{accountId}',
            path: {
                'accountId': accountId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Delete request validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                404: `Account not found`,
                409: `Delete eligibility conflict`,
            },
        });
    }
}
