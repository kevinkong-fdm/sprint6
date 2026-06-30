/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
import type { StatementLineItem } from './StatementLineItem';
export type MonthlyStatement = {
    accountId: string;
    customerId: string;
    month: string;
    timezone: string;
    openingBalance: MoneyAmount;
    closingBalance: MoneyAmount;
    totalDebits: MoneyAmount;
    totalCredits: MoneyAmount;
    lineItems: Array<StatementLineItem>;
    generatedAt: string;
};

