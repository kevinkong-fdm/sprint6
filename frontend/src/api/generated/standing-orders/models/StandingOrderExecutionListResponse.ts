/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { StandingOrderExecution } from './StandingOrderExecution';
export type StandingOrderExecutionListResponse = {
    correlationId: string;
    timestamp: string;
    data: {
        items: Array<StandingOrderExecution>;
        page: number;
        size: number;
        total: number;
    };
};

