import { apiRequest } from "../../../api/client";

export type StatementLineItem = {
  transactionId: string;
  postedAt: string;
  entryType: "DEBIT" | "CREDIT";
  amount: string;
  balanceAfter: string;
  description: string;
};

export type MonthlyStatement = {
  accountId: string;
  customerId: string;
  month: string;
  timezone: string;
  openingBalance: string;
  closingBalance: string;
  totalDebits: string;
  totalCredits: string;
  lineItems: StatementLineItem[];
  generatedAt: string;
};

type Envelope<T> = {
  correlationId: string;
  timestamp: string;
  data: T;
};

export async function generateMonthlyStatement(
  accessToken: string,
  accountId: string,
  month: string,
) {
  const response = await apiRequest<Envelope<MonthlyStatement>>("/statements/monthly/generate", {
    method: "POST",
    body: JSON.stringify({ accountId, month }),
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function getMonthlyStatement(
  accessToken: string,
  accountId: string,
  month: string,
) {
  const query = new URLSearchParams({ accountId, month });
  const response = await apiRequest<Envelope<MonthlyStatement>>(
    `/statements/monthly?${query.toString()}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  return response.data;
}

export async function listGeneratedMonthlyStatements(
  accessToken: string,
  accountId: string,
  fallbackMonth?: string,
) {
  const query = new URLSearchParams({ accountId });
  try {
    const response = await apiRequest<Envelope<MonthlyStatement[]>>(
      `/statements/monthly/history?${query.toString()}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      },
    );

    return response.data;
  } catch (error) {
    if (isHistoryEndpointUnavailable(error) && fallbackMonth?.trim()) {
      const single = await getMonthlyStatement(accessToken, accountId, fallbackMonth.trim());
      return [single];
    }

    throw error;
  }
}

function isHistoryEndpointUnavailable(error: unknown): boolean {
  if (typeof error !== "object" || error === null) {
    return false;
  }

  const record = error as Record<string, unknown>;
  const status = typeof record.status === "number" ? record.status : 0;
  const message = typeof record.message === "string" ? record.message : "";
  const path = typeof record.path === "string" ? record.path : "";

  if (status !== 404) {
    return false;
  }

  const normalizedMessage = message.toLowerCase();
  const normalizedPath = path.toLowerCase();

  return (
    normalizedMessage.includes("no static resource") ||
    normalizedMessage.includes("statements/monthly/history") ||
    normalizedPath.includes("/statements/monthly/history")
  );
}
