/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type StandingOrder = {
    standingOrderId: string;
    customerId: string;
    sourceAccountId: string;
    destinationAccountId: string;
    amount: MoneyAmount;
    frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY';
    startDate: string;
    endDate?: string;
    executionDayOfWeek?: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
    executionDayOfMonth?: number;
    executionTime?: string;
    status: 'ACTIVE' | 'PAUSED' | 'CANCELED';
    timezone: string;
    nextExecutionAt?: string | null;
    lastExecutionAt?: string | null;
    createdAt: string;
    updatedAt: string;
};

