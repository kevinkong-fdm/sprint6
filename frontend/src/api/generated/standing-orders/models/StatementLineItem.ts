/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type StatementLineItem = {
    transactionId: string;
    postedAt: string;
    entryType: 'DEBIT' | 'CREDIT';
    amount: MoneyAmount;
    balanceAfter: MoneyAmount;
    description: string;
};

