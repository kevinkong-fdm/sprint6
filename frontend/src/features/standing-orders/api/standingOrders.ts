import { apiRequest } from "../../../api/client";

export type StandingOrderFrequency = "DAILY" | "WEEKLY" | "MONTHLY";
export type StandingOrderStatus = "ACTIVE" | "PAUSED" | "CANCELED";
export type StandingOrderOutcome = "SUCCESS" | "FAILED" | "SKIPPED";

export type StandingOrder = {
  standingOrderId: string;
  customerId: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: string;
  frequency: StandingOrderFrequency;
  startDate: string;
  endDate?: string;
  executionDayOfWeek?: string;
  executionDayOfMonth?: number;
  executionTime: string;
  status: StandingOrderStatus;
  timezone: string;
  nextExecutionAt?: string;
  lastExecutionAt?: string;
  createdAt: string;
  updatedAt: string;
};

export type StandingOrderExecution = {
  standingOrderExecutionId: string;
  standingOrderId: string;
  scheduledFor: string;
  triggeredAt: string;
  outcome: StandingOrderOutcome;
  failureReasonCode?: string;
  transferReferenceId?: string;
  correlationId: string;
};

export type CreateStandingOrderRequest = {
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  frequency: StandingOrderFrequency;
  startDate: string;
  endDate?: string;
  executionDayOfWeek?: string;
  executionDayOfMonth?: number;
  executionTime?: string;
};

export type UpdateStandingOrderRequest = {
  destinationAccountId?: string;
  amount?: number;
  frequency?: StandingOrderFrequency;
  endDate?: string;
  executionDayOfWeek?: string;
  executionDayOfMonth?: number;
  executionTime?: string;
};

type Envelope<T> = {
  correlationId: string;
  timestamp: string;
  data: T;
};

type PaginatedData<T> = {
  items: T[];
  page: number;
  size: number;
  total: number;
};

export async function createStandingOrder(
  accessToken: string,
  payload: CreateStandingOrderRequest,
  idempotencyKey?: string,
) {
  const headers: Record<string, string> = {
    Authorization: `Bearer ${accessToken}`,
  };

  if (idempotencyKey?.trim()) {
    headers["Idempotency-Key"] = idempotencyKey.trim();
  }

  const response = await apiRequest<Envelope<StandingOrder>>("/standing-orders", {
    method: "POST",
    body: JSON.stringify(payload),
    headers,
  });

  return response.data;
}

export async function listStandingOrders(
  accessToken: string,
  options?: { status?: StandingOrderStatus; page?: number; size?: number },
) {
  const query = new URLSearchParams();
  if (options?.status) {
    query.set("status", options.status);
  }
  if (options?.page) {
    query.set("page", String(options.page));
  }
  if (options?.size) {
    query.set("size", String(options.size));
  }

  const path = query.toString() ? `/standing-orders?${query.toString()}` : "/standing-orders";
  const response = await apiRequest<Envelope<PaginatedData<StandingOrder>>>(path, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function getStandingOrder(accessToken: string, standingOrderId: string) {
  const response = await apiRequest<Envelope<StandingOrder>>(`/standing-orders/${standingOrderId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function updateStandingOrder(
  accessToken: string,
  standingOrderId: string,
  payload: UpdateStandingOrderRequest,
) {
  const response = await apiRequest<Envelope<StandingOrder>>(`/standing-orders/${standingOrderId}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function pauseStandingOrder(accessToken: string, standingOrderId: string) {
  const response = await apiRequest<Envelope<StandingOrder>>(`/standing-orders/${standingOrderId}/pause`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function resumeStandingOrder(accessToken: string, standingOrderId: string) {
  const response = await apiRequest<Envelope<StandingOrder>>(`/standing-orders/${standingOrderId}/resume`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function cancelStandingOrder(accessToken: string, standingOrderId: string) {
  const response = await apiRequest<Envelope<StandingOrder>>(`/standing-orders/${standingOrderId}/cancel`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}

export async function triggerStandingOrderExecution(
  accessToken: string,
  standingOrderId: string,
  reason?: string,
  idempotencyKey?: string,
) {
  const headers: Record<string, string> = {
    Authorization: `Bearer ${accessToken}`,
  };

  if (idempotencyKey?.trim()) {
    headers["Idempotency-Key"] = idempotencyKey.trim();
  }

  const response = await apiRequest<Envelope<StandingOrderExecution>>(
    `/standing-orders/${standingOrderId}/executions/trigger`,
    {
      method: "POST",
      body: reason ? JSON.stringify({ reason }) : undefined,
      headers,
    },
  );

  return response.data;
}

export async function listStandingOrderExecutions(
  accessToken: string,
  standingOrderId: string,
  options?: { outcome?: StandingOrderOutcome; page?: number; size?: number },
) {
  const query = new URLSearchParams();
  if (options?.outcome) {
    query.set("outcome", options.outcome);
  }
  if (options?.page) {
    query.set("page", String(options.page));
  }
  if (options?.size) {
    query.set("size", String(options.size));
  }

  const path = query.toString()
    ? `/standing-orders/${standingOrderId}/executions?${query.toString()}`
    : `/standing-orders/${standingOrderId}/executions`;

  const response = await apiRequest<Envelope<PaginatedData<StandingOrderExecution>>>(path, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}
