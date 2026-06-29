/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type MovementResponse = {
    movementId: string;
    accountId: string;
    movementType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER_DEBIT' | 'TRANSFER_CREDIT' | 'CLOSEOUT_DEBIT' | 'CLOSEOUT_CREDIT';
    amount: MoneyAmount;
    balanceAfter: MoneyAmount;
    postedAt: string;
};

