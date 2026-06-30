/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type UpdateStandingOrderRequest = {
    destinationAccountId?: string;
    amount?: MoneyAmount;
    frequency?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
    endDate?: string;
    executionDayOfWeek?: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
    executionDayOfMonth?: number;
    executionTime?: string;
};

