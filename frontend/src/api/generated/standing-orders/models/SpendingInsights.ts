/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { SpendingCategoryMetric } from './SpendingCategoryMetric';
import type { SpendingTotals } from './SpendingTotals';
export type SpendingInsights = {
    periodStart: string;
    periodEnd: string;
    comparisonMode: 'PREVIOUS_PERIOD' | 'NONE';
    /**
     * True when history is insufficient for full comparison.
     */
    insufficientData: boolean;
    /**
     * Explicit insufficiency indicator for low-data success responses.
     */
    insufficiencyReason?: 'INSUFFICIENT_HISTORY' | 'NONE';
    categories: Array<SpendingCategoryMetric>;
    totals: SpendingTotals;
};

