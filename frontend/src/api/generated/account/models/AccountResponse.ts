/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type AccountResponse = {
    accountId: string;
    customerId: string;
    accountType: 'CHECKING' | 'SAVINGS';
    nickname?: string;
    currencyCode: string;
    availableBalance: MoneyAmount;
    ledgerBalance: MoneyAmount;
    status: 'ACTIVE' | 'PENDING_DELETE';
    createdAt: string;
    updatedAt: string;
};

