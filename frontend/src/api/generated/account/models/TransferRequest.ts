/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MoneyAmount } from './MoneyAmount';
export type TransferRequest = {
    sourceAccountId: string;
    destinationAccountId: string;
    amount: MoneyAmount;
    idempotencyKey?: string;
};

