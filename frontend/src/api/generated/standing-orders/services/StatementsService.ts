/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { GenerateMonthlyStatementRequest } from '../models/GenerateMonthlyStatementRequest';
import type { MonthlyStatementSingleResponse } from '../models/MonthlyStatementSingleResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class StatementsService {
    /**
     * Generate monthly statement
     * Generates a statement for a closed month and authorized account using fixed AEST month boundaries. Valid no-activity months return successful statements with zeroed totals.
     * @returns MonthlyStatementSingleResponse Statement generated
     * @throws ApiError
     */
    public static generateMonthlyStatement({
        requestBody,
    }: {
        requestBody: GenerateMonthlyStatementRequest,
    }): CancelablePromise<MonthlyStatementSingleResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/statements/monthly/generate',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Statement period validation failure`,
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Statement unavailable for account or period`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Retrieve monthly statement
     * @returns MonthlyStatementSingleResponse Statement retrieved
     * @throws ApiError
     */
    public static getMonthlyStatement({
        accountId,
        month,
    }: {
        accountId: string,
        /**
         * Calendar month in YYYY-MM evaluated in fixed AEST semantics.
         */
        month: string,
    }): CancelablePromise<MonthlyStatementSingleResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/statements/monthly',
            query: {
                'accountId': accountId,
                'month': month,
            },
            errors: {
                400: `Statement period validation failure`,
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Statement unavailable for account or period`,
                500: `Unexpected feature service error`,
            },
        });
    }
}
