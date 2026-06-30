/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
export type StandingOrderExecution = {
    standingOrderExecutionId: string;
    standingOrderId: string;
    scheduledFor: string;
    triggeredAt: string;
    outcome: 'SUCCESS' | 'FAILED' | 'SKIPPED';
    failureReasonCode?: 'SO-EXE-001' | 'SO-EXE-002';
    transferReferenceId?: string;
    correlationId: string;
};

