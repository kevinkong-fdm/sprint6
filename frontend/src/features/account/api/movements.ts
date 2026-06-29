import { apiRequest } from "../../../api/client";

export type MovementType =
  | "DEPOSIT"
  | "WITHDRAWAL"
  | "TRANSFER_DEBIT"
  | "TRANSFER_CREDIT"
  | "CLOSEOUT_DEBIT"
  | "CLOSEOUT_CREDIT";

export type MovementStatus = "PENDING" | "POSTED" | "FAILED";

export type DepositRequest = {
  amount: number;
  idempotencyKey?: string;
};

export type WithdrawalRequest = {
  amount: number;
  idempotencyKey?: string;
};

export type MovementResponse = {
  movementId: string;
  accountId: string;
  movementType: MovementType;
  amount: string;
  balanceAfter: string;
  postedAt: string;
};

export type TransferRequest = {
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  idempotencyKey?: string;
};

export type TransferResponse = {
  transferId: string;
  sourceMovement: MovementResponse;
  destinationMovement: MovementResponse;
};

export type TransactionHistoryQuery = {
  from?: string;
  to?: string;
  type?: MovementType;
  page?: number;
  size?: number;
};

export type TransactionHistoryItemResponse = {
  movementId: string;
  movementType: MovementType;
  amount: string;
  balanceAfter: string;
  status: MovementStatus;
  createdAt: string;
  postedAt?: string;
  referenceId?: string;
};

export type TransactionHistoryResponse = {
  accountId: string;
  items: TransactionHistoryItemResponse[];
  page: number;
  size: number;
  total: number;
};

export async function depositFunds(accessToken: string, accountId: string, payload: DepositRequest) {
  return apiRequest<MovementResponse>(`/accounts/${accountId}/deposits`, {
    method: "POST",
    body: JSON.stringify(payload),
    headers: authHeaders(accessToken),
  });
}

export async function withdrawFunds(accessToken: string, accountId: string, payload: WithdrawalRequest) {
  return apiRequest<MovementResponse>(`/accounts/${accountId}/withdrawals`, {
    method: "POST",
    body: JSON.stringify(payload),
    headers: authHeaders(accessToken),
  });
}

export async function transferFunds(accessToken: string, payload: TransferRequest) {
  return apiRequest<TransferResponse>("/transfers", {
    method: "POST",
    body: JSON.stringify(payload),
    headers: authHeaders(accessToken),
  });
}

export async function getTransactionHistory(
  accessToken: string,
  accountId: string,
  query: TransactionHistoryQuery = {},
) {
  const params = new URLSearchParams();

  if (query.from) {
    params.set("from", query.from);
  }
  if (query.to) {
    params.set("to", query.to);
  }
  if (query.type) {
    params.set("type", query.type);
  }
  if (query.page !== undefined) {
    params.set("page", `${query.page}`);
  }
  if (query.size !== undefined) {
    params.set("size", `${query.size}`);
  }

  const suffix = params.toString() ? `?${params.toString()}` : "";
  return apiRequest<TransactionHistoryResponse>(`/accounts/${accountId}/transactions${suffix}`, {
    method: "GET",
    headers: authHeaders(accessToken),
  });
}

function authHeaders(accessToken: string) {
  return {
    Authorization: `Bearer ${accessToken}`,
  };
}
