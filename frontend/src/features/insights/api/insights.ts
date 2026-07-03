import { apiRequest } from "../../../api/client";

export type SpendingInsightsCategory = {
  category: string;
  currentTotal: string;
  previousTotal?: string;
  deltaAmount?: string;
  deltaPercent?: number;
};

export type SpendingInsightsTotals = {
  currentTotal: string;
  previousTotal?: string;
  deltaAmount?: string;
  deltaPercent?: number;
};

export type SpendingInsights = {
  periodStart: string;
  periodEnd: string;
  categories: SpendingInsightsCategory[];
  totals: SpendingInsightsTotals;
};

type Envelope<T> = {
  correlationId: string;
  timestamp: string;
  data: T;
};

export async function getSpendingInsights(
  accessToken: string,
  options: {
    periodStart: string;
    periodEnd: string;
    accountId?: string;
  },
) {
  const query = new URLSearchParams({
    periodStart: options.periodStart,
    periodEnd: options.periodEnd,
    comparisonMode: "PREVIOUS_PERIOD",
  });

  if (options.accountId?.trim()) {
    query.set("accountId", options.accountId.trim());
  }

  const response = await apiRequest<Envelope<SpendingInsights>>(`/insights/spending?${query.toString()}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}
