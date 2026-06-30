/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateStandingOrderRequest } from '../models/CreateStandingOrderRequest';
import type { StandingOrderExecutionListResponse } from '../models/StandingOrderExecutionListResponse';
import type { StandingOrderExecutionSingleResponse } from '../models/StandingOrderExecutionSingleResponse';
import type { StandingOrderListResponse } from '../models/StandingOrderListResponse';
import type { StandingOrderSingleResponse } from '../models/StandingOrderSingleResponse';
import type { TriggerExecutionRequest } from '../models/TriggerExecutionRequest';
import type { UpdateStandingOrderRequest } from '../models/UpdateStandingOrderRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class StandingOrdersService {
    /**
     * Create standing order
     * Creates a standing order for an authenticated customer. Destination must be an internal platform account owned by the same authenticated customer as the source account.
     * @returns StandingOrderSingleResponse Standing order created
     * @throws ApiError
     */
    public static createStandingOrder({
        requestBody,
        idempotencyKey,
    }: {
        requestBody: CreateStandingOrderRequest,
        /**
         * Client-provided idempotency token. Reused key with equivalent request semantics returns prior committed result.
         */
        idempotencyKey?: string,
    }): CancelablePromise<StandingOrderSingleResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/standing-orders',
            headers: {
                'Idempotency-Key': idempotencyKey,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Setup validation failure`,
                401: `Authentication required or session invalid`,
                403: `Source account unauthorized`,
                409: `Destination conflicts`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * List standing orders
     * Returns standing orders for the authenticated customer. Ordering is deterministic by `nextExecutionAt` ascending then `standingOrderId` ascending. Supports pagination and status filtering.
     * @returns StandingOrderListResponse Standing orders listed
     * @throws ApiError
     */
    public static listStandingOrders({
        status,
        page = 1,
        size = 20,
    }: {
        status?: 'ACTIVE' | 'PAUSED' | 'CANCELED',
        page?: number,
        size?: number,
    }): CancelablePromise<StandingOrderListResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/standing-orders',
            query: {
                'status': status,
                'page': page,
                'size': size,
            },
            errors: {
                401: `Authentication required or session invalid`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Retrieve standing order
     * @returns StandingOrderSingleResponse Standing order found
     * @throws ApiError
     */
    public static getStandingOrder({
        standingOrderId,
    }: {
        standingOrderId: string,
    }): CancelablePromise<StandingOrderSingleResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/standing-orders/{standingOrderId}',
            path: {
                'standingOrderId': standingOrderId,
            },
            errors: {
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Update standing order
     * Only mutable standing-order fields may be updated.
     * @returns StandingOrderSingleResponse Standing order updated
     * @throws ApiError
     */
    public static updateStandingOrder({
        standingOrderId,
        requestBody,
    }: {
        standingOrderId: string,
        requestBody: UpdateStandingOrderRequest,
    }): CancelablePromise<StandingOrderSingleResponse> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/standing-orders/{standingOrderId}',
            path: {
                'standingOrderId': standingOrderId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Update validation failure`,
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                422: `Immutable field update attempted`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Pause standing order
     * @returns StandingOrderSingleResponse Standing order paused
     * @throws ApiError
     */
    public static pauseStandingOrder({
        standingOrderId,
    }: {
        standingOrderId: string,
    }): CancelablePromise<StandingOrderSingleResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/standing-orders/{standingOrderId}/pause',
            path: {
                'standingOrderId': standingOrderId,
            },
            errors: {
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Resume standing order
     * @returns StandingOrderSingleResponse Standing order resumed
     * @throws ApiError
     */
    public static resumeStandingOrder({
        standingOrderId,
    }: {
        standingOrderId: string,
    }): CancelablePromise<StandingOrderSingleResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/standing-orders/{standingOrderId}/resume',
            path: {
                'standingOrderId': standingOrderId,
            },
            errors: {
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Cancel standing order
     * @returns StandingOrderSingleResponse Standing order canceled
     * @throws ApiError
     */
    public static cancelStandingOrder({
        standingOrderId,
    }: {
        standingOrderId: string,
    }): CancelablePromise<StandingOrderSingleResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/standing-orders/{standingOrderId}/cancel',
            path: {
                'standingOrderId': standingOrderId,
            },
            errors: {
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * Trigger standing-order execution attempt
     * Triggers an execution attempt. Duplicate trigger requests with the same idempotency key must not create duplicate postings.
     * @returns StandingOrderExecutionSingleResponse Execution attempt recorded
     * @throws ApiError
     */
    public static triggerStandingOrderExecution({
        standingOrderId,
        idempotencyKey,
        requestBody,
    }: {
        standingOrderId: string,
        /**
         * Client-provided idempotency token. Reused key with equivalent request semantics returns prior committed result.
         */
        idempotencyKey?: string,
        requestBody?: TriggerExecutionRequest,
    }): CancelablePromise<StandingOrderExecutionSingleResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/standing-orders/{standingOrderId}/executions/trigger',
            path: {
                'standingOrderId': standingOrderId,
            },
            headers: {
                'Idempotency-Key': idempotencyKey,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                409: `Execution conflict`,
                500: `Unexpected feature service error`,
            },
        });
    }
    /**
     * List standing-order executions
     * Returns execution history ordered by `triggeredAt` descending then `standingOrderExecutionId` descending.
     * @returns StandingOrderExecutionListResponse Execution history listed
     * @throws ApiError
     */
    public static listStandingOrderExecutions({
        standingOrderId,
        outcome,
        page = 1,
        size = 20,
    }: {
        standingOrderId: string,
        outcome?: 'SUCCESS' | 'FAILED' | 'SKIPPED',
        page?: number,
        size?: number,
    }): CancelablePromise<StandingOrderExecutionListResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/standing-orders/{standingOrderId}/executions',
            path: {
                'standingOrderId': standingOrderId,
            },
            query: {
                'outcome': outcome,
                'page': page,
                'size': size,
            },
            errors: {
                401: `Authentication required or session invalid`,
                403: `Authenticated caller is forbidden for target resource`,
                404: `Standing order not found`,
                500: `Unexpected feature service error`,
            },
        });
    }
}
