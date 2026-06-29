/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type TransactionHistoryItem = {
    movementId: string;
    movementType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER_DEBIT' | 'TRANSFER_CREDIT' | 'CLOSEOUT_DEBIT' | 'CLOSEOUT_CREDIT';
    amount: MoneyAmount;
    balanceAfter: MoneyAmount;
    status: 'PENDING' | 'POSTED' | 'FAILED';
    createdAt: string;
    postedAt?: string;
    referenceId?: string;
};

