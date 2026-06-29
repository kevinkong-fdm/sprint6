/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { TransactionHistoryResponse } from '../models/TransactionHistoryResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TransactionsService {
    /**
     * Get transaction history
     * @returns TransactionHistoryResponse Transaction history returned
     * @throws ApiError
     */
    public static getTransactionHistory({
        accountId,
        from,
        to,
        type,
        page = 1,
        size = 50,
    }: {
        accountId: string,
        from?: string,
        to?: string,
        type?: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER_DEBIT' | 'TRANSFER_CREDIT' | 'CLOSEOUT_DEBIT' | 'CLOSEOUT_CREDIT',
        page?: number,
        size?: number,
    }): CancelablePromise<TransactionHistoryResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/accounts/{accountId}/transactions',
            path: {
                'accountId': accountId,
            },
            query: {
                'from': from,
                'to': to,
                'type': type,
                'page': page,
                'size': size,
            },
            errors: {
                400: `Transaction history filter validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                404: `Account not found`,
            },
        });
    }
}
