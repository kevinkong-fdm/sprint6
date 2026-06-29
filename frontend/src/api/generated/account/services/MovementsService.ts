/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DepositRequest } from '../models/DepositRequest';
import type { MovementResponse } from '../models/MovementResponse';
import type { WithdrawalRequest } from '../models/WithdrawalRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class MovementsService {
    /**
     * Deposit funds
     * @returns MovementResponse Deposit posted
     * @throws ApiError
     */
    public static depositFunds({
        accountId,
        requestBody,
    }: {
        accountId: string,
        requestBody: DepositRequest,
    }): CancelablePromise<MovementResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/accounts/{accountId}/deposits',
            path: {
                'accountId': accountId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Deposit validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                404: `Account not found`,
            },
        });
    }
    /**
     * Withdraw funds
     * @returns MovementResponse Withdrawal posted
     * @throws ApiError
     */
    public static withdrawFunds({
        accountId,
        requestBody,
    }: {
        accountId: string,
        requestBody: WithdrawalRequest,
    }): CancelablePromise<MovementResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/accounts/{accountId}/withdrawals',
            path: {
                'accountId': accountId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Withdrawal validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                404: `Account not found`,
                409: `Insufficient funds`,
            },
        });
    }
}
