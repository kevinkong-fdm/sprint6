import { FormEvent, useEffect, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import { listAccounts } from "../../account/api/accounts";
import { StandingOrder, listStandingOrders } from "../../standing-orders/api/standingOrders";
import {
  NotificationDispatchStatus,
  NotificationEvent,
  NotificationEventType,
  listStandingOrderNotifications,
} from "../api/notifications";
import { isSessionRecoveryRequired, mapNotificationApiError } from "../api/errorMapper";

const eventTypeOptions: NotificationEventType[] = [
  "LIFECYCLE_UPDATED",
  "EXECUTION_SUCCESS",
  "EXECUTION_FAILURE",
  "EXECUTION_SKIPPED",
];

const dispatchStatusOptions: NotificationDispatchStatus[] = ["PENDING", "SENT", "FAILED"];

export function NotificationsPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [eventTypeFilter, setEventTypeFilter] = useState<"" | NotificationEventType>("");
  const [dispatchStatusFilter, setDispatchStatusFilter] = useState<"" | NotificationDispatchStatus>("");
  const [page, setPage] = useState("1");
  const [size, setSize] = useState("20");

  const [notifications, setNotifications] = useState<NotificationEvent[]>([]);
  const [total, setTotal] = useState(0);
  const [accountNamesById, setAccountNamesById] = useState<Record<string, string>>({});
  const [standingOrdersById, setStandingOrdersById] = useState<Record<string, StandingOrder>>({});

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!hasAccessToken) {
      return;
    }
    void loadNotifications();
  }, [hasAccessToken]);

  useEffect(() => {
    if (!hasAccessToken) {
      setAccountNamesById({});
      setStandingOrdersById({});
      return;
    }

    void loadReferenceData();
  }, [hasAccessToken]);

  async function loadNotifications(event?: FormEvent) {
    event?.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    const resolvedPage = clampInteger(page, 1, 1, 5000);
    const resolvedSize = clampInteger(size, 20, 1, 200);

    setIsLoading(true);
    try {
      const response = await listStandingOrderNotifications(accessToken, {
        eventType: eventTypeFilter || undefined,
        dispatchStatus: dispatchStatusFilter || undefined,
        page: resolvedPage,
        size: resolvedSize,
      });

      setNotifications(response.items);
      setTotal(response.total);
      setPage(String(resolvedPage));
      setSize(String(resolvedSize));
      setStatus(`Loaded ${response.items.length} notification events.`);
    } catch (err) {
      handleNotificationsError(err);
    } finally {
      setIsLoading(false);
    }
  }

  function handleNotificationsError(err: unknown) {
    const mapped = mapNotificationApiError(err);
    setError(mapped.message);

    if (isSessionRecoveryRequired(mapped.code)) {
      signOut();
    }
  }

  async function loadReferenceData() {
    if (!hasAccessToken) {
      return;
    }

    try {
      const [accountsResponse, standingOrdersResponse] = await Promise.all([
        listAccounts(accessToken, {
          page: 1,
          size: 200,
        }),
        listStandingOrders(accessToken, {
          page: 1,
          size: 200,
        }),
      ]);

      const accountMap = accountsResponse.items.reduce<Record<string, string>>((acc, account) => {
        const normalizedName = (account.nickname ?? "").trim();
        if (normalizedName) {
          acc[account.accountId] = normalizedName;
        }
        return acc;
      }, {});

      const standingOrderMap = standingOrdersResponse.items.reduce<Record<string, StandingOrder>>((acc, order) => {
        acc[order.standingOrderId] = order;
        return acc;
      }, {});

      setAccountNamesById(accountMap);
      setStandingOrdersById(standingOrderMap);
    } catch {
      // Keep notification feed usable even if metadata lookups are unavailable.
    }
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Notifications</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Standing order event feed</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Review lifecycle and execution notifications.
        </p>
      </header>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <h3 className="text-xl font-semibold text-white">Filter notifications</h3>
        <form onSubmit={loadNotifications} className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          <select
            value={eventTypeFilter}
            onChange={(event) => setEventTypeFilter(event.target.value as "" | NotificationEventType)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          >
            <option value="">All event types</option>
            {eventTypeOptions.map((eventType) => (
              <option key={eventType} value={eventType}>
                {eventType}
              </option>
            ))}
          </select>

          <select
            value={dispatchStatusFilter}
            onChange={(event) => setDispatchStatusFilter(event.target.value as "" | NotificationDispatchStatus)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          >
            <option value="">All dispatch states</option>
            {dispatchStatusOptions.map((dispatchStatus) => (
              <option key={dispatchStatus} value={dispatchStatus}>
                {dispatchStatus}
              </option>
            ))}
          </select>

          <input
            type="number"
            min="1"
            value={page}
            onChange={(event) => setPage(event.target.value)}
            placeholder="Page"
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />

          <input
            type="number"
            min="1"
            max="200"
            value={size}
            onChange={(event) => setSize(event.target.value)}
            placeholder="Page size"
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />

          <button
            type="submit"
            disabled={!hasAccessToken || isLoading}
            className="inline-flex items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
          >
            {isLoading ? "Loading..." : "Load notifications"}
          </button>
        </form>

        <p className="mt-4 text-sm text-slate-300">Total records: {total}</p>

        {notifications.length > 0 ? (
          <div className="mt-4 overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
            <table className="min-w-full text-left text-xs text-slate-200">
              <thead className="border-b border-white/10 text-slate-400">
                <tr>
                  <th className="px-3 py-2 font-semibold">Event</th>
                  <th className="px-3 py-2 font-semibold">Standing order</th>
                  <th className="px-3 py-2 font-semibold">Dispatch</th>
                  <th className="px-3 py-2 font-semibold">Created</th>
                  <th className="px-3 py-2 font-semibold">Message</th>
                </tr>
              </thead>
              <tbody>
                {notifications.map((notification) => (
                  <tr key={notification.notificationEventId} className="border-b border-white/5 align-top">
                    <td className="px-3 py-2">{notification.eventType}</td>
                    <td className="px-3 py-2" title={notification.standingOrderId ?? ""}>
                      {formatReference("SO", notification.standingOrderId)}
                    </td>
                    <td className="px-3 py-2">{notification.dispatchStatus}</td>
                    <td className="px-3 py-2">{formatTimestamp(notification.createdAt)}</td>
                    <td className="px-3 py-2 text-slate-300">
                      {formatNotificationMessage(notification, standingOrdersById, accountNamesById)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
            No notifications loaded for the current filter set.
          </p>
        )}
      </article>

      {status ? (
        <p role="status" className="rounded-xl border border-emerald-300/30 bg-emerald-400/10 px-4 py-3 text-sm text-emerald-100">
          {status}
        </p>
      ) : null}

      {error ? (
        <p role="alert" className="rounded-xl border border-rose-300/30 bg-rose-400/10 px-4 py-3 text-sm text-rose-100">
          {error}
        </p>
      ) : null}
    </section>
  );
}

function clampInteger(value: string, fallback: number, min: number, max: number): number {
  const parsed = Number.parseInt(value, 10);
  if (Number.isNaN(parsed)) {
    return fallback;
  }
  return Math.min(max, Math.max(min, parsed));
}

function formatTimestamp(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function formatReference(prefix: string, rawValue?: string): string {
  if (!rawValue || !rawValue.trim()) {
    return "-";
  }

  const normalized = rawValue.replace(/[^a-zA-Z0-9]/g, "").toUpperCase();
  if (!normalized) {
    return "-";
  }

  if (normalized.length <= 8) {
    return `${prefix}-${normalized}`;
  }

  return `${prefix}-${normalized.slice(0, 4)}-${normalized.slice(-4)}`;
}

function formatNotificationMessage(
  notification: NotificationEvent,
  standingOrdersById: Record<string, StandingOrder>,
  accountNamesById: Record<string, string>,
): string {
  const fallback = (notification.message ?? "").trim();
  if (!fallback) {
    return "-";
  }

  const standingOrderId = notification.standingOrderId?.trim();
  if (!standingOrderId) {
    return fallback;
  }

  const standingOrderAlias = formatReference("SO", standingOrderId);
  let normalizedMessage = fallback
    .replace(new RegExp(`standing order\\s+${escapeRegExp(standingOrderId)}`, "gi"), `Standing order ${standingOrderAlias}`)
    .split(standingOrderId)
    .join(standingOrderAlias);

  normalizedMessage = normalizedMessage.replace(/\bstanding order\b/gi, "Standing order");

  const order = standingOrdersById[standingOrderId];
  if (!order) {
    return normalizedMessage;
  }

  const sourceAccountLabel = resolveAccountLabel(order.sourceAccountId, accountNamesById);
  const destinationAccountLabel = resolveAccountLabel(order.destinationAccountId, accountNamesById);

  normalizedMessage = normalizedMessage.replace(
    /\([^()]*\s->\s[^()]*\)/,
    `(${sourceAccountLabel} -> ${destinationAccountLabel})`,
  );

  return normalizedMessage;
}

function resolveAccountLabel(accountId: string, accountNamesById: Record<string, string>): string {
  const mappedName = (accountNamesById[accountId] ?? "").trim();
  if (mappedName) {
    return mappedName;
  }

  return formatReference("ACCT", accountId);
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
