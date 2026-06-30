/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { SpendingInsightsSingleResponse } from '../models/SpendingInsightsSingleResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class InsightsService {
    /**
     * Retrieve spending insights
     * Returns categorized spending totals and trend outputs. For valid filters with insufficient history, returns HTTP 200 with `insufficientData=true` and partial metrics instead of an error.
     * @returns SpendingInsightsSingleResponse Insights returned
     * @throws ApiError
     */
    public static getSpendingInsights({
        periodStart,
        periodEnd,
        comparisonMode = 'PREVIOUS_PERIOD',
        accountId,
    }: {
        periodStart: string,
        periodEnd: string,
        comparisonMode?: 'PREVIOUS_PERIOD' | 'NONE',
        accountId?: string,
    }): CancelablePromise<SpendingInsightsSingleResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/insights/spending',
            query: {
                'periodStart': periodStart,
                'periodEnd': periodEnd,
                'comparisonMode': comparisonMode,
                'accountId': accountId,
            },
            errors: {
                400: `Insights filter validation failure`,
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                500: `Unexpected feature service error`,
            },
        });
    }
}
