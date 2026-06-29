import { apiRequest } from "../../../api/client";

export type AccountType = "CHECKING" | "SAVINGS";
export type AccountStatus = "ACTIVE" | "PENDING_DELETE";

export type CreateAccountRequest = {
  accountType: AccountType;
  nickname?: string;
};

export type UpdateAccountRequest = {
  nickname: string;
};

export type DeleteAccountRequest = {
  idempotencyKey?: string;
};

export type AccountResponse = {
  accountId: string;
  customerId: string;
  accountType: AccountType;
  nickname?: string;
  currencyCode: string;
  availableBalance: string;
  ledgerBalance: string;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string;
};

export type AccountListResponse = {
  items: AccountResponse[];
  page: number;
  size: number;
  total: number;
};

export async function createAccount(accessToken: string, payload: CreateAccountRequest) {
  return apiRequest<AccountResponse>("/accounts", {
    method: "POST",
    body: JSON.stringify(payload),
    headers: authHeaders(accessToken),
  });
}

export async function listAccounts(
  accessToken: string,
  query: { accountType?: AccountType; page?: number; size?: number } = {},
) {
  const params = new URLSearchParams();
  if (query.accountType) {
    params.set("accountType", query.accountType);
  }
  if (query.page !== undefined) {
    params.set("page", `${query.page}`);
  }
  if (query.size !== undefined) {
    params.set("size", `${query.size}`);
  }

  const suffix = params.toString() ? `?${params.toString()}` : "";
  return apiRequest<AccountListResponse>(`/accounts${suffix}`, {
    method: "GET",
    headers: authHeaders(accessToken),
  });
}

export async function getAccount(accessToken: string, accountId: string) {
  return apiRequest<AccountResponse>(`/accounts/${accountId}`, {
    method: "GET",
    headers: authHeaders(accessToken),
  });
}

export async function updateAccount(accessToken: string, accountId: string, payload: UpdateAccountRequest) {
  return apiRequest<AccountResponse>(`/accounts/${accountId}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
    headers: authHeaders(accessToken),
  });
}

export async function deleteAccount(accessToken: string, accountId: string, payload?: DeleteAccountRequest) {
  const safePayload = payload
    ? {
        idempotencyKey: payload.idempotencyKey?.trim() || undefined,
      }
    : undefined;

  const hasPayload = Boolean(safePayload?.idempotencyKey);

  return apiRequest<void>(`/accounts/${accountId}`, {
    method: "DELETE",
    body: hasPayload ? JSON.stringify(safePayload) : undefined,
    headers: authHeaders(accessToken),
  });
}

function authHeaders(accessToken: string) {
  return {
    Authorization: `Bearer ${accessToken}`,
  };
}
