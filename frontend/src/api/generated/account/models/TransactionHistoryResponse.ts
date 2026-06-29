/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { TransactionHistoryItem } from './TransactionHistoryItem';
export type TransactionHistoryResponse = {
    accountId: string;
    items: Array<TransactionHistoryItem>;
    page: number;
    size: number;
    total: number;
};

