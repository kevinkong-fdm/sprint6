/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type NotificationEvent = {
    notificationEventId: string;
    standingOrderId?: string;
    standingOrderExecutionId?: string;
    eventType: 'LIFECYCLE_UPDATED' | 'EXECUTION_SUCCESS' | 'EXECUTION_FAILURE' | 'EXECUTION_SKIPPED';
    title: string;
    message: string;
    dispatchStatus: 'PENDING' | 'SENT' | 'FAILED';
    dispatchAttemptCount?: number;
    createdAt: string;
    dispatchedAt?: string | null;
};

