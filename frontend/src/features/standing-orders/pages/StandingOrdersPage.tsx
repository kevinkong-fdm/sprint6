import { FormEvent, useEffect, useMemo, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import {
  CreateStandingOrderRequest,
  StandingOrder,
  StandingOrderExecution,
  StandingOrderFrequency,
  StandingOrderOutcome,
  StandingOrderStatus,
  cancelStandingOrder,
  createStandingOrder,
  getStandingOrder,
  listStandingOrderExecutions,
  listStandingOrders,
  pauseStandingOrder,
  resumeStandingOrder,
  triggerStandingOrderExecution,
  updateStandingOrder,
} from "../api/standingOrders";
import { isSessionRecoveryRequired, mapStandingOrderApiError } from "../api/errorMapper";
import { AccountResponse, listAccounts } from "../../account/api/accounts";

const frequencyOptions: StandingOrderFrequency[] = ["DAILY", "WEEKLY", "MONTHLY"];
const statusOptions: StandingOrderStatus[] = ["ACTIVE", "PAUSED", "CANCELED"];
const dayOfWeekOptions = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
const executionOutcomeOptions: StandingOrderOutcome[] = ["SUCCESS", "FAILED", "SKIPPED"];

export function StandingOrdersPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [sourceAccountId, setSourceAccountId] = useState("");
  const [destinationAccountId, setDestinationAccountId] = useState("");
  const [amount, setAmount] = useState("");
  const [frequency, setFrequency] = useState<StandingOrderFrequency>("MONTHLY");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [executionDayOfWeek, setExecutionDayOfWeek] = useState("");
  const [executionDayOfMonth, setExecutionDayOfMonth] = useState("");
  const [executionTime, setExecutionTime] = useState("09:00");

  const [statusFilter, setStatusFilter] = useState<"" | StandingOrderStatus>("");
  const [orders, setOrders] = useState<StandingOrder[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<StandingOrder | null>(null);
  const [selectedOrderId, setSelectedOrderId] = useState("");

  const [updateDestinationAccountId, setUpdateDestinationAccountId] = useState("");
  const [updateAmount, setUpdateAmount] = useState("");
  const [updateFrequency, setUpdateFrequency] = useState<"" | StandingOrderFrequency>("");
  const [updateEndDate, setUpdateEndDate] = useState("");
  const [updateExecutionDayOfWeek, setUpdateExecutionDayOfWeek] = useState("");
  const [updateExecutionDayOfMonth, setUpdateExecutionDayOfMonth] = useState("");
  const [updateExecutionTime, setUpdateExecutionTime] = useState("");

  const [triggerReason, setTriggerReason] = useState("");
  const [executionOutcomeFilter, setExecutionOutcomeFilter] = useState<"" | StandingOrderOutcome>("");
  const [executions, setExecutions] = useState<StandingOrderExecution[]>([]);
  const [accountNamesById, setAccountNamesById] = useState<Record<string, string>>({});

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [isLoadingOrders, setIsLoadingOrders] = useState(false);
  const [isCreatingOrder, setIsCreatingOrder] = useState(false);
  const [isLoadingSelected, setIsLoadingSelected] = useState(false);
  const [isUpdatingOrder, setIsUpdatingOrder] = useState(false);
  const [isPausing, setIsPausing] = useState(false);
  const [isResuming, setIsResuming] = useState(false);
  const [isCanceling, setIsCanceling] = useState(false);
  const [isTriggering, setIsTriggering] = useState(false);
  const [isLoadingExecutions, setIsLoadingExecutions] = useState(false);

  const isBusy =
    isLoadingOrders ||
    isCreatingOrder ||
    isLoadingSelected ||
    isUpdatingOrder ||
    isPausing ||
    isResuming ||
    isCanceling ||
    isTriggering ||
    isLoadingExecutions;

  const canLoadSelected = selectedOrderId.trim().length > 0 && !isBusy && hasAccessToken;
  const canPauseOrder = Boolean(selectedOrder) && selectedOrder?.status === "ACTIVE" && !isBusy && hasAccessToken;
  const canResumeOrder = Boolean(selectedOrder) && selectedOrder?.status === "PAUSED" && !isBusy && hasAccessToken;
  const canCancelOrder = Boolean(selectedOrder) && selectedOrder?.status !== "CANCELED" && !isBusy && hasAccessToken;

  const canUpdateOrder = useMemo(() => {
    if (!selectedOrder || isBusy || !hasAccessToken) {
      return false;
    }

    return (
      updateDestinationAccountId.trim().length > 0 ||
      updateAmount.trim().length > 0 ||
      updateFrequency.length > 0 ||
      updateEndDate.trim().length > 0 ||
      updateExecutionDayOfWeek.trim().length > 0 ||
      updateExecutionDayOfMonth.trim().length > 0 ||
      updateExecutionTime.trim().length > 0
    );
  }, [
    selectedOrder,
    isBusy,
    hasAccessToken,
    updateDestinationAccountId,
    updateAmount,
    updateFrequency,
    updateEndDate,
    updateExecutionDayOfWeek,
    updateExecutionDayOfMonth,
    updateExecutionTime,
  ]);

  useEffect(() => {
    if (!hasAccessToken) {
      setAccountNamesById({});
      return;
    }

    void loadOrders();
    void loadAccountDirectory();
  }, [hasAccessToken]);

  useEffect(() => {
    if (!selectedOrder) {
      return;
    }

    setUpdateDestinationAccountId(selectedOrder.destinationAccountId ?? "");
    setUpdateAmount(selectedOrder.amount ?? "");
    setUpdateFrequency(selectedOrder.frequency ?? "");
    setUpdateEndDate(selectedOrder.endDate ?? "");
    setUpdateExecutionDayOfWeek(selectedOrder.executionDayOfWeek ?? "");
    setUpdateExecutionDayOfMonth(
      selectedOrder.executionDayOfMonth !== undefined ? String(selectedOrder.executionDayOfMonth) : "",
    );
    setUpdateExecutionTime(selectedOrder.executionTime ?? "");
  }, [selectedOrder]);

  async function loadOrders() {
    if (!hasAccessToken) {
      return;
    }

    setIsLoadingOrders(true);
    try {
      const response = await listStandingOrders(accessToken, {
        status: statusFilter || undefined,
        page: 1,
        size: 100,
      });
      setOrders(response.items);
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsLoadingOrders(false);
    }
  }

  async function loadAccountDirectory() {
    if (!hasAccessToken) {
      return;
    }

    try {
      const response = await listAccounts(accessToken, {
        page: 1,
        size: 200,
      });

      const accountMap = response.items.reduce<Record<string, string>>((acc, account: AccountResponse) => {
        const normalizedName = (account.nickname ?? "").trim();
        if (normalizedName) {
          acc[account.accountId] = normalizedName;
        }
        return acc;
      }, {});

      setAccountNamesById(accountMap);
    } catch {
      // Keep standing-order operations functional even if account metadata fails to load.
    }
  }

  async function loadSelectedOrder(orderId: string) {
    if (!hasAccessToken) {
      return;
    }

    const trimmedOrderId = orderId.trim();
    if (!trimmedOrderId) {
      setError("Enter a standing-order ID to load.");
      return;
    }

    setIsLoadingSelected(true);
    try {
      const loaded = await getStandingOrder(accessToken, trimmedOrderId);
      setSelectedOrder(loaded);
      setSelectedOrderId(loaded.standingOrderId);
      setStatus(`Loaded standing order ${formatStandingOrderLabel(loaded.standingOrderId)}.`);
      setError("");
      await loadExecutions(loaded.standingOrderId);
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsLoadingSelected(false);
    }
  }

  async function loadExecutions(orderId: string) {
    if (!hasAccessToken) {
      return;
    }

    setIsLoadingExecutions(true);
    try {
      const response = await listStandingOrderExecutions(accessToken, orderId, {
        outcome: executionOutcomeFilter || undefined,
        page: 1,
        size: 50,
      });
      setExecutions(response.items);
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsLoadingExecutions(false);
    }
  }

  async function handleCreateOrder(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    const normalizedAmount = parsePositiveAmount(amount);
    if (normalizedAmount === null) {
      setError("Enter a valid standing-order amount greater than 0.");
      return;
    }

    if (!startDate.trim()) {
      setError("Provide a start date for standing order setup.");
      return;
    }

    const payload: CreateStandingOrderRequest = {
      sourceAccountId: sourceAccountId.trim(),
      destinationAccountId: destinationAccountId.trim(),
      amount: normalizedAmount,
      frequency,
      startDate,
      endDate: normalizeOptional(endDate),
      executionDayOfWeek: normalizeOptional(executionDayOfWeek),
      executionDayOfMonth:
        frequency === "MONTHLY" ? parseOptionalInteger(executionDayOfMonth) ?? undefined : undefined,
      executionTime: normalizeOptional(executionTime),
    };

    setIsCreatingOrder(true);
    try {
      const created = await createStandingOrder(
        accessToken,
        payload,
        createIdempotencyKey("standing-order-create"),
      );
      setStatus(`Created standing order ${formatStandingOrderLabel(created.standingOrderId)}.`);
      setSourceAccountId("");
      setDestinationAccountId("");
      setAmount("");
      setStartDate("");
      setEndDate("");
      setExecutionDayOfWeek("");
      setExecutionDayOfMonth("");
      setExecutionTime("09:00");
      setFrequency("MONTHLY");

      await loadOrders();
      await loadSelectedOrder(created.standingOrderId);
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsCreatingOrder(false);
    }
  }

  async function handleUpdateOrder(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedOrder) {
      setError("Load a standing order before applying updates.");
      return;
    }

    const amountValue = updateAmount.trim().length > 0 ? parsePositiveAmount(updateAmount) : null;
    if (updateAmount.trim().length > 0 && amountValue === null) {
      setError("Enter a valid updated amount greater than 0.");
      return;
    }

    const updatePayload = {
      destinationAccountId: normalizeOptional(updateDestinationAccountId),
      amount: amountValue ?? undefined,
      frequency: updateFrequency || undefined,
      endDate: normalizeOptional(updateEndDate),
      executionDayOfWeek: normalizeOptional(updateExecutionDayOfWeek),
      executionDayOfMonth: parseOptionalInteger(updateExecutionDayOfMonth) ?? undefined,
      executionTime: normalizeOptional(updateExecutionTime),
    };

    setIsUpdatingOrder(true);
    try {
      const updated = await updateStandingOrder(
        accessToken,
        selectedOrder.standingOrderId,
        updatePayload,
      );
      setSelectedOrder(updated);
      setStatus(`Updated standing order ${formatStandingOrderLabel(updated.standingOrderId)}.`);
      await loadOrders();
      await loadExecutions(updated.standingOrderId);
    } catch (err) {
      handleStandingOrderError(err, "update");
    } finally {
      setIsUpdatingOrder(false);
    }
  }

  async function handlePauseOrder() {
    if (!hasAccessToken || !selectedOrder) {
      return;
    }

    if (selectedOrder.status !== "ACTIVE") {
      setStatus(`Standing order ${formatStandingOrderLabel(selectedOrder.standingOrderId)} is already ${selectedOrder.status.toLowerCase()}.`);
      return;
    }

    setStatus("");
    setError("");
    setIsPausing(true);

    try {
      const updated = await pauseStandingOrder(accessToken, selectedOrder.standingOrderId);
      setSelectedOrder(updated);
      setOrders((current) =>
        current.map((order) => (order.standingOrderId === updated.standingOrderId ? updated : order)),
      );
      const filterHint = statusFilter === "ACTIVE" ? " It may no longer appear under the ACTIVE filter." : "";
      setStatus(`Paused standing order ${formatStandingOrderLabel(updated.standingOrderId)}.${filterHint}`);
      await loadOrders();
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsPausing(false);
    }
  }

  async function handleResumeOrder() {
    if (!hasAccessToken || !selectedOrder) {
      return;
    }

    if (selectedOrder.status !== "PAUSED") {
      setStatus(`Standing order ${formatStandingOrderLabel(selectedOrder.standingOrderId)} is ${selectedOrder.status.toLowerCase()} and cannot be resumed.`);
      return;
    }

    setStatus("");
    setError("");
    setIsResuming(true);

    try {
      const updated = await resumeStandingOrder(accessToken, selectedOrder.standingOrderId);
      setSelectedOrder(updated);
      setOrders((current) =>
        current.map((order) => (order.standingOrderId === updated.standingOrderId ? updated : order)),
      );
      const filterHint = statusFilter === "PAUSED" ? " It may no longer appear under the PAUSED filter." : "";
      setStatus(`Resumed standing order ${formatStandingOrderLabel(updated.standingOrderId)}.${filterHint}`);
      await loadOrders();
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsResuming(false);
    }
  }

  async function handleCancelOrder() {
    if (!hasAccessToken || !selectedOrder) {
      return;
    }

    if (selectedOrder.status === "CANCELED") {
      setStatus(`Standing order ${formatStandingOrderLabel(selectedOrder.standingOrderId)} is already canceled.`);
      return;
    }

    setStatus("");
    setError("");
    setIsCanceling(true);

    try {
      const updated = await cancelStandingOrder(accessToken, selectedOrder.standingOrderId);
      setSelectedOrder(updated);
      setOrders((current) =>
        current.map((order) => (order.standingOrderId === updated.standingOrderId ? updated : order)),
      );
      const filterHint = statusFilter === "ACTIVE" || statusFilter === "PAUSED"
        ? " It may no longer appear under the current status filter."
        : "";
      setStatus(`Canceled standing order ${formatStandingOrderLabel(updated.standingOrderId)}.${filterHint}`);
      await loadOrders();
    } catch (err) {
      handleStandingOrderError(err);
    } finally {
      setIsCanceling(false);
    }
  }

  async function handleTriggerExecution(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedOrder) {
      setError("Load a standing order before triggering execution.");
      return;
    }

    setIsTriggering(true);
    try {
      const execution = await triggerStandingOrderExecution(
        accessToken,
        selectedOrder.standingOrderId,
        normalizeOptional(triggerReason),
        createIdempotencyKey("standing-order-trigger"),
      );
      setStatus(`Triggered execution ${formatStandingOrderLabel(execution.standingOrderId)}.`);
      setTriggerReason("");
      await loadSelectedOrder(selectedOrder.standingOrderId);
    } catch (err) {
      const mapped = mapStandingOrderApiError(err);
      const contextMessage = `Unable to trigger execution: ${mapped.message}`;
      if (isSessionRecoveryRequired(mapped.code)) {
        signOut();
      }
      await loadSelectedOrder(selectedOrder.standingOrderId);
      setError(contextMessage);
    } finally {
      setIsTriggering(false);
    }
  }

  function handleStandingOrderError(err: unknown, context?: "update" | "trigger") {
    const mapped = mapStandingOrderApiError(err);
    const contextPrefix =
      context === "trigger"
        ? "Unable to trigger execution"
        : context === "update"
          ? "Unable to update standing order"
          : "Standing-order action failed";
    setError(`${contextPrefix}: ${mapped.message}`);

    if (isSessionRecoveryRequired(mapped.code)) {
      signOut();
    }
  }

  function resolveAccountName(accountId: string): string {
    const knownName = accountNamesById[accountId];
    if (knownName) {
      return knownName;
    }
    return formatAccountLabel(accountId);
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Standing orders</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Recurring payment automation</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Create, maintain, and trigger authenticated standing orders across your internal accounts.
        </p>
        {!hasAccessToken ? (
          <p className="mt-4 rounded-xl border border-amber-300/30 bg-amber-400/10 px-4 py-3 text-sm text-amber-100">
            Session token is missing. Sign out and sign in again before running standing-order operations.
          </p>
        ) : null}
      </header>

      <div className="grid gap-6 xl:grid-cols-3">
        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Create standing order</h3>
          <form onSubmit={handleCreateOrder} className="mt-4 space-y-3">
            <input
              aria-label="Source account ID"
              type="text"
              required
              value={sourceAccountId}
              onChange={(event) => setSourceAccountId(event.target.value)}
              placeholder="Source account ID"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <input
              aria-label="Destination account ID"
              type="text"
              required
              value={destinationAccountId}
              onChange={(event) => setDestinationAccountId(event.target.value)}
              placeholder="Destination account ID"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <input
              aria-label="Standing-order amount"
              type="number"
              step="0.01"
              min="0.01"
              required
              value={amount}
              onChange={(event) => setAmount(event.target.value)}
              placeholder="Amount"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <select
              aria-label="Standing-order frequency"
              value={frequency}
              onChange={(event) => setFrequency(event.target.value as StandingOrderFrequency)}
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            >
              {frequencyOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
            <input
              aria-label="Standing-order start date"
              type="date"
              required
              value={startDate}
              onChange={(event) => setStartDate(event.target.value)}
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <input
              aria-label="Standing-order end date"
              type="date"
              value={endDate}
              onChange={(event) => setEndDate(event.target.value)}
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            {frequency === "WEEKLY" ? (
              <select
                aria-label="Execution day of week"
                value={executionDayOfWeek}
                onChange={(event) => setExecutionDayOfWeek(event.target.value)}
                className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
              >
                <option value="">Select execution weekday</option>
                {dayOfWeekOptions.map((day) => (
                  <option key={day} value={day}>
                    {day}
                  </option>
                ))}
              </select>
            ) : null}
            {frequency === "MONTHLY" ? (
              <input
                aria-label="Execution day of month"
                type="number"
                min="1"
                max="28"
                value={executionDayOfMonth}
                onChange={(event) => setExecutionDayOfMonth(event.target.value)}
                placeholder="Execution day of month (1-28)"
                className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
              />
            ) : null}
            <input
              aria-label="Execution time"
              type="time"
              value={executionTime}
              onChange={(event) => setExecutionTime(event.target.value)}
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!hasAccessToken || isBusy}
              className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
            >
              {isCreatingOrder ? "Creating..." : "Create standing order"}
            </button>
          </form>
        </article>

        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Standing-order list</h3>
          <div className="mt-4 flex flex-wrap gap-2">
            <select
              aria-label="Standing-order status filter"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value as "" | StandingOrderStatus)}
              className="rounded-full border border-white/20 bg-slate-950/70 px-3 py-2 text-xs font-semibold text-slate-100 focus:border-cyan-300 focus:outline-none"
            >
              <option value="">All statuses</option>
              {statusOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() => {
                void loadOrders();
              }}
              disabled={!hasAccessToken || isBusy}
              className="rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoadingOrders ? "Refreshing..." : "Refresh"}
            </button>
          </div>

          <form
            onSubmit={(event) => {
              event.preventDefault();
              void loadSelectedOrder(selectedOrderId);
            }}
            className="mt-4 space-y-3"
          >
            <input
              aria-label="Load standing order ID"
              type="text"
              value={selectedOrderId}
              onChange={(event) => setSelectedOrderId(event.target.value)}
              placeholder="Order reference ID (full)"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!canLoadSelected}
              className="inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isLoadingSelected ? "Loading..." : "Load standing order"}
            </button>
          </form>

          <div className="mt-4 space-y-2">
            {orders.length === 0 ? (
              <p className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
                No standing orders loaded yet.
              </p>
            ) : (
              orders.map((order) => (
                <article
                  key={order.standingOrderId}
                  className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 transition hover:border-cyan-300/40 hover:bg-slate-900/70"
                >
                  <p className="text-xs uppercase tracking-[0.14em] text-slate-400">{order.status}</p>
                  <p className="mt-1 text-sm font-semibold text-white">{formatStandingOrderLabel(order.standingOrderId)}</p>
                  <p className="mt-1 text-xs text-slate-300">
                    {resolveAccountName(order.sourceAccountId)} to {resolveAccountName(order.destinationAccountId)}
                  </p>
                  <p className="mt-1 text-xs text-slate-300">
                    {order.frequency} • ${formatMoney(order.amount)} • Next {formatTimestamp(order.nextExecutionAt)}
                  </p>
                  <button
                    type="button"
                    onClick={() => {
                      void loadSelectedOrder(order.standingOrderId);
                    }}
                    className="mt-3 rounded-full bg-cyan-300 px-3 py-1.5 text-xs font-semibold text-slate-950 transition hover:bg-cyan-200"
                  >
                    Load order
                  </button>
                </article>
              ))
            )}
          </div>
        </article>

        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Selected standing order</h3>
          {selectedOrder ? (
            <div className="mt-4 space-y-4">
              <div className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200">
                <p className="font-semibold text-white">{formatStandingOrderLabel(selectedOrder.standingOrderId)}</p>
                <p className="mt-1 break-all text-xs text-slate-400">Reference ID: {selectedOrder.standingOrderId}</p>
                <p className="mt-1 text-xs text-slate-300">
                  {selectedOrder.frequency} • {selectedOrder.status} • {resolveAccountName(selectedOrder.sourceAccountId)} to {resolveAccountName(selectedOrder.destinationAccountId)}
                </p>
                <p className="mt-1 text-xs text-slate-300">Next execution {formatTimestamp(selectedOrder.nextExecutionAt)}</p>
              </div>

              <form onSubmit={handleUpdateOrder} className="space-y-3">
                <input
                  aria-label="Update destination account ID"
                  type="text"
                  value={updateDestinationAccountId}
                  onChange={(event) => setUpdateDestinationAccountId(event.target.value)}
                  placeholder="Destination account ID (optional update)"
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <input
                  aria-label="Update amount"
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={updateAmount}
                  onChange={(event) => setUpdateAmount(event.target.value)}
                  placeholder="Amount"
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <select
                  aria-label="Update frequency"
                  value={updateFrequency}
                  onChange={(event) => setUpdateFrequency(event.target.value as "" | StandingOrderFrequency)}
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                >
                  <option value="">Keep existing frequency</option>
                  {frequencyOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
                <input
                  aria-label="Update end date"
                  type="date"
                  value={updateEndDate}
                  onChange={(event) => setUpdateEndDate(event.target.value)}
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <select
                  aria-label="Update day of week"
                  value={updateExecutionDayOfWeek}
                  onChange={(event) => setUpdateExecutionDayOfWeek(event.target.value)}
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                >
                  <option value="">Keep existing weekday</option>
                  {dayOfWeekOptions.map((day) => (
                    <option key={day} value={day}>
                      {day}
                    </option>
                  ))}
                </select>
                <input
                  aria-label="Update day of month"
                  type="number"
                  min="1"
                  max="28"
                  value={updateExecutionDayOfMonth}
                  onChange={(event) => setUpdateExecutionDayOfMonth(event.target.value)}
                  placeholder="Execution day of month"
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <input
                  aria-label="Update execution time"
                  type="time"
                  value={updateExecutionTime}
                  onChange={(event) => setUpdateExecutionTime(event.target.value)}
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <button
                  type="submit"
                  disabled={!canUpdateOrder}
                  className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
                >
                  {isUpdatingOrder ? "Updating..." : "Update standing order"}
                </button>
              </form>

              <div className="grid gap-2 sm:grid-cols-3">
                <button
                  type="button"
                  onClick={() => {
                    void handlePauseOrder();
                  }}
                  disabled={!canPauseOrder}
                  className="rounded-xl bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isPausing ? "Pausing..." : "Pause"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    void handleResumeOrder();
                  }}
                  disabled={!canResumeOrder}
                  className="rounded-xl bg-emerald-300 px-3 py-2 text-xs font-semibold text-slate-950 transition hover:bg-emerald-200 disabled:cursor-not-allowed disabled:bg-emerald-200/70"
                >
                  {isResuming ? "Resuming..." : "Resume"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    void handleCancelOrder();
                  }}
                  disabled={!canCancelOrder}
                  className="rounded-xl bg-rose-300 px-3 py-2 text-xs font-semibold text-slate-950 transition hover:bg-rose-200 disabled:cursor-not-allowed disabled:bg-rose-200/70"
                >
                  {isCanceling ? "Canceling..." : selectedOrder.status === "CANCELED" ? "Canceled" : "Cancel"}
                </button>
              </div>

              <form onSubmit={handleTriggerExecution} className="space-y-3 border-t border-white/10 pt-4">
                <input
                  aria-label="Execution reason"
                  type="text"
                  value={triggerReason}
                  onChange={(event) => setTriggerReason(event.target.value)}
                  placeholder="Optional execution reason"
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <button
                  type="submit"
                  disabled={!hasAccessToken || isBusy}
                  className="inline-flex w-full items-center justify-center rounded-xl bg-indigo-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-indigo-200 disabled:cursor-not-allowed disabled:bg-indigo-200/70"
                >
                  {isTriggering ? "Triggering..." : "Trigger execution"}
                </button>
              </form>
            </div>
          ) : (
            <p className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
              Load a standing order to update lifecycle state and run execution triggers.
            </p>
          )}
        </article>
      </div>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h3 className="text-xl font-semibold text-white">Execution history</h3>
          <div className="flex items-center gap-2">
            <select
              aria-label="Execution outcome filter"
              value={executionOutcomeFilter}
              onChange={(event) => setExecutionOutcomeFilter(event.target.value as "" | StandingOrderOutcome)}
              className="rounded-full border border-white/20 bg-slate-950/70 px-3 py-2 text-xs font-semibold text-slate-100 focus:border-cyan-300 focus:outline-none"
            >
              <option value="">All outcomes</option>
              {executionOutcomeOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() => {
                if (!selectedOrder) {
                  return;
                }
                void loadExecutions(selectedOrder.standingOrderId);
              }}
              disabled={!selectedOrder || !hasAccessToken || isBusy}
              className="rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoadingExecutions ? "Refreshing..." : "Refresh"}
            </button>
          </div>
        </div>

        {executions.length > 0 ? (
          <div className="mt-4 overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
            <table className="min-w-full text-left text-xs text-slate-200">
              <thead className="border-b border-white/10 text-slate-400">
                <tr>
                  <th className="px-3 py-2 font-semibold">Outcome</th>
                  <th className="px-3 py-2 font-semibold">Scheduled</th>
                  <th className="px-3 py-2 font-semibold">Triggered</th>
                  <th className="px-3 py-2 font-semibold">Reference</th>
                </tr>
              </thead>
              <tbody>
                {executions.map((execution) => (
                  <tr key={execution.standingOrderExecutionId} className="border-b border-white/5">
                    <td className="px-3 py-2">{execution.outcome}</td>
                    <td className="px-3 py-2">{formatTimestamp(execution.scheduledFor)}</td>
                    <td className="px-3 py-2">{formatTimestamp(execution.triggeredAt)}</td>
                    <td className="px-3 py-2">{execution.transferReferenceId ?? execution.failureReasonCode ?? "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
            {selectedOrder
              ? "No executions yet for the selected standing order."
              : "Load a standing order to inspect execution history."}
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

function normalizeOptional(value: string): string | undefined {
  const trimmed = value.trim();
  return trimmed ? trimmed : undefined;
}

function parsePositiveAmount(value: string): number | null {
  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed) || parsed <= 0) {
    return null;
  }

  return Number(parsed.toFixed(2));
}

function parseOptionalInteger(value: string): number | null {
  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }

  const parsed = Number.parseInt(trimmed, 10);
  if (Number.isNaN(parsed)) {
    return null;
  }

  return parsed;
}

function formatMoney(value: string): string {
  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed)) {
    return "0.00";
  }
  return parsed.toFixed(2);
}

function formatTimestamp(value?: string): string {
  if (!value) {
    return "-";
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function createIdempotencyKey(prefix: string): string {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return `${prefix}-${crypto.randomUUID()}`;
  }
  return `${prefix}-${Date.now()}`;
}

function formatStandingOrderLabel(standingOrderId: string): string {
  return buildReferenceLabel("SO", standingOrderId);
}

function formatAccountLabel(accountId: string): string {
  return buildReferenceLabel("ACCT", accountId);
}

function buildReferenceLabel(prefix: string, rawId: string): string {
  const normalized = rawId.replace(/[^a-zA-Z0-9]/g, "").toUpperCase();

  if (!normalized) {
    return `${prefix}-UNKNOWN`;
  }

  if (normalized.length <= 8) {
    return `${prefix}-${normalized}`;
  }

  return `${prefix}-${normalized.slice(0, 4)}-${normalized.slice(-4)}`;
}

