import { apiRequest } from "../../../api/client";

export type NotificationEventType =
  | "LIFECYCLE_UPDATED"
  | "EXECUTION_SUCCESS"
  | "EXECUTION_FAILURE"
  | "EXECUTION_SKIPPED";

export type NotificationDispatchStatus = "PENDING" | "SENT" | "FAILED";

export type NotificationEvent = {
  notificationEventId: string;
  standingOrderId?: string;
  standingOrderExecutionId?: string;
  eventType: NotificationEventType;
  title: string;
  message: string;
  dispatchStatus: NotificationDispatchStatus;
  dispatchAttemptCount: number;
  createdAt: string;
  dispatchedAt?: string;
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

export async function listStandingOrderNotifications(
  accessToken: string,
  options?: {
    eventType?: NotificationEventType;
    dispatchStatus?: NotificationDispatchStatus;
    page?: number;
    size?: number;
  },
) {
  const query = new URLSearchParams();
  if (options?.eventType) {
    query.set("eventType", options.eventType);
  }
  if (options?.dispatchStatus) {
    query.set("dispatchStatus", options.dispatchStatus);
  }
  if (options?.page) {
    query.set("page", String(options.page));
  }
  if (options?.size) {
    query.set("size", String(options.size));
  }

  const path = query.toString()
    ? `/notifications/standing-orders?${query.toString()}`
    : "/notifications/standing-orders";

  const response = await apiRequest<Envelope<PaginatedData<NotificationEvent>>>(path, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return response.data;
}
