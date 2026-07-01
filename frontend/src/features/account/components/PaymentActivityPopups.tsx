import { useEffect, useRef, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import { AccountResponse, listAccounts } from "../api/accounts";
import { isSessionRecoveryRequired, mapAccountApiError } from "../api/errorMapper";
import {
  MovementType,
  TransactionHistoryItemResponse,
  getTransactionHistory,
} from "../api/movements";

type PaymentToast = {
  id: string;
  message: string;
  expiresAt: number;
};

type AccountMovementSnapshot = {
  account: AccountResponse;
  item: TransactionHistoryItemResponse;
};

const POLL_INTERVAL_MS = 12000;
const TOAST_TTL_MS = 7000;
const MAX_TOASTS = 4;
const HISTORY_PAGE_SIZE = 12;
const PAYMENT_MOVEMENT_TYPES: ReadonlySet<MovementType> = new Set([
  "TRANSFER_DEBIT",
  "TRANSFER_CREDIT",
  "DEPOSIT",
  "WITHDRAWAL",
]);

export function PaymentActivityPopups() {
  const { session, isAuthenticated, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [toasts, setToasts] = useState<PaymentToast[]>([]);

  const seenMovementIdsRef = useRef<Set<string>>(new Set());
  const hasPrimedRef = useRef(false);
  const pollingInFlightRef = useRef(false);

  useEffect(() => {
    if (!isAuthenticated || !hasAccessToken) {
      setToasts([]);
      seenMovementIdsRef.current.clear();
      hasPrimedRef.current = false;
      pollingInFlightRef.current = false;
      return;
    }

    let isCancelled = false;

    const pollPaymentActivity = async () => {
      if (isCancelled || pollingInFlightRef.current) {
        return;
      }

      pollingInFlightRef.current = true;
      try {
        const accountList = await listAccounts(accessToken, { page: 1, size: 100 });
        const snapshots = await collectLatestPaymentSnapshots(accessToken, accountList.items);
        snapshots.sort(sortSnapshots);

        if (!hasPrimedRef.current) {
          for (const snapshot of snapshots) {
            seenMovementIdsRef.current.add(snapshot.item.movementId);
          }
          hasPrimedRef.current = true;
          return;
        }

        const unseenSnapshots = snapshots.filter(
          (snapshot) => !seenMovementIdsRef.current.has(snapshot.item.movementId),
        );

        if (unseenSnapshots.length === 0) {
          return;
        }

        for (const snapshot of unseenSnapshots) {
          seenMovementIdsRef.current.add(snapshot.item.movementId);
        }

        const nextToasts: PaymentToast[] = unseenSnapshots.slice(-MAX_TOASTS).map((snapshot) => ({
          id: `${snapshot.item.movementId}-${movementTimestamp(snapshot.item)}`,
          message: buildToastMessage(snapshot.account, snapshot.item),
          expiresAt: Date.now() + TOAST_TTL_MS,
        }));

        if (!isCancelled) {
          setToasts((current) => [...current, ...nextToasts].slice(-MAX_TOASTS));
        }
      } catch (err) {
        const mapped = mapAccountApiError(err);
        if (isSessionRecoveryRequired(mapped.code)) {
          signOut();
        }
      } finally {
        pollingInFlightRef.current = false;
      }
    };

    void pollPaymentActivity();
    const intervalId = window.setInterval(() => {
      void pollPaymentActivity();
    }, POLL_INTERVAL_MS);

    return () => {
      isCancelled = true;
      window.clearInterval(intervalId);
    };
  }, [accessToken, hasAccessToken, isAuthenticated, signOut]);

  useEffect(() => {
    if (toasts.length === 0) {
      return;
    }

    const intervalId = window.setInterval(() => {
      const now = Date.now();
      setToasts((current) => current.filter((toast) => toast.expiresAt > now));
    }, 500);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [toasts.length]);

  if (!isAuthenticated || toasts.length === 0) {
    return null;
  }

  return (
    <div
      aria-live="polite"
      aria-atomic="true"
      className="pointer-events-none fixed right-4 top-24 z-[60] flex w-[min(26rem,calc(100vw-2rem))] flex-col gap-3"
    >
      {toasts.map((toast) => (
        <article
          key={toast.id}
          className="animate-fade-up rounded-2xl border border-emerald-300/35 bg-slate-900/90 p-4 shadow-soft backdrop-blur-xl"
        >
          <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-emerald-200/85">
            Payment update
          </p>
          <p className="mt-1 text-sm font-medium text-slate-100">{toast.message}</p>
        </article>
      ))}
    </div>
  );
}

async function collectLatestPaymentSnapshots(
  accessToken: string,
  accounts: AccountResponse[],
): Promise<AccountMovementSnapshot[]> {
  const snapshotGroups = await Promise.all(
    accounts.map(async (account) => {
      const history = await getTransactionHistory(accessToken, account.accountId, {
        page: 1,
        size: HISTORY_PAGE_SIZE,
      });

      return history.items
        .filter((item) => PAYMENT_MOVEMENT_TYPES.has(item.movementType))
        .map((item) => ({ account, item }));
    }),
  );

  return snapshotGroups.flat();
}

function sortSnapshots(a: AccountMovementSnapshot, b: AccountMovementSnapshot): number {
  const timeDiff = movementTimestamp(a.item) - movementTimestamp(b.item);
  if (timeDiff !== 0) {
    return timeDiff;
  }

  return a.item.movementId.localeCompare(b.item.movementId);
}

function movementTimestamp(item: TransactionHistoryItemResponse): number {
  const candidate = item.postedAt ?? item.createdAt;
  const timestamp = Date.parse(candidate);
  return Number.isNaN(timestamp) ? 0 : timestamp;
}

function buildToastMessage(
  account: AccountResponse,
  movement: TransactionHistoryItemResponse,
): string {
  const amount = formatCurrencyAmount(movement.amount, account.currencyCode);
  const accountLabel = resolveAccountLabel(account);

  if (isIncomingMovement(movement.movementType)) {
    return `${amount} received to account ${accountLabel}`;
  }

  return `${amount} sent from account ${accountLabel}`;
}

function isIncomingMovement(movementType: MovementType): boolean {
  return movementType === "TRANSFER_CREDIT" || movementType === "DEPOSIT";
}

function formatCurrencyAmount(amount: string, currencyCode: string): string {
  const parsed = Number(amount);
  if (!Number.isFinite(parsed)) {
    return `$${amount}`;
  }

  try {
    return new Intl.NumberFormat("en-AU", {
      style: "currency",
      currency: currencyCode,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(parsed);
  } catch {
    return `$${parsed.toFixed(2)}`;
  }
}

function resolveAccountLabel(account: AccountResponse): string {
  const nickname = account.nickname?.trim();
  if (nickname) {
    return nickname;
  }

  return abbreviateAccountId(account.accountId);
}

function abbreviateAccountId(accountId: string): string {
  const value = accountId.trim();
  if (value.length <= 10) {
    return value;
  }

  return `${value.slice(0, 6)}...${value.slice(-4)}`;
}
