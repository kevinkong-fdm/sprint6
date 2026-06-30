/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type CreateStandingOrderRequest = {
    sourceAccountId: string;
    /**
     * Internal platform account ID owned by the same customer.
     */
    destinationAccountId: string;
    amount: MoneyAmount;
    frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY';
    startDate: string;
    endDate?: string;
    executionDayOfWeek?: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
    executionDayOfMonth?: number;
    /**
     * HH:mm in fixed AEST semantics.
     */
    executionTime?: string;
    /**
     * Unsupported in this version. If provided, the request is rejected with SO-SET-004.
     * @deprecated
     */
    externalBankDetails?: Record<string, any>;
};

