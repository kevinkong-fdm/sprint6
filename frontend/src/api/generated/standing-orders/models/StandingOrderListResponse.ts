/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { StandingOrder } from './StandingOrder';
export type StandingOrderListResponse = {
    correlationId: string;
    timestamp: string;
    data: {
        items: Array<StandingOrder>;
        page: number;
        size: number;
        total: number;
    };
};

