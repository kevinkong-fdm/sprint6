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
