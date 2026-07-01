import { FormEvent, useEffect, useMemo, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import {
  AccountResponse,
  AccountType,
  createAccount,
  deleteAccount,
  getAccount,
  listAccounts,
  updateAccount,
} from "../api/accounts";
import { isSessionRecoveryRequired, mapAccountApiError } from "../api/errorMapper";
import {
  MovementType,
  TransactionHistoryResponse,
  depositFunds,
  getTransactionHistory,
  transferFunds,
  withdrawFunds,
} from "../api/movements";

const accountTypeOptions: AccountType[] = ["CHECKING", "SAVINGS"];
const historyTypeOptions: MovementType[] = [
  "DEPOSIT",
  "WITHDRAWAL",
  "TRANSFER_DEBIT",
  "TRANSFER_CREDIT",
  "CLOSEOUT_DEBIT",
  "CLOSEOUT_CREDIT",
];

export function AccountsPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<AccountResponse | null>(null);
  const [selectedAccountId, setSelectedAccountId] = useState("");

  const [createAccountType, setCreateAccountType] = useState<AccountType>("CHECKING");
  const [createNickname, setCreateNickname] = useState("");

  const [accountTypeFilter, setAccountTypeFilter] = useState<"" | AccountType>("");

  const [updateNickname, setUpdateNickname] = useState("");

  const [depositAmount, setDepositAmount] = useState("");
  const [withdrawAmount, setWithdrawAmount] = useState("");

  const [transferSourceAccountId, setTransferSourceAccountId] = useState("");
  const [transferDestinationAccountId, setTransferDestinationAccountId] = useState("");
  const [transferAmount, setTransferAmount] = useState("");

  const [historyFrom, setHistoryFrom] = useState("");
  const [historyTo, setHistoryTo] = useState("");
  const [historyType, setHistoryType] = useState<"" | MovementType>("");
  const [historyPage, setHistoryPage] = useState("1");
  const [historySize, setHistorySize] = useState("50");
  const [history, setHistory] = useState<TransactionHistoryResponse | null>(null);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [copiedAccountId, setCopiedAccountId] = useState<string | null>(null);

  const [isLoadingAccounts, setIsLoadingAccounts] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [isLoadingAccount, setIsLoadingAccount] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isDepositing, setIsDepositing] = useState(false);
  const [isWithdrawing, setIsWithdrawing] = useState(false);
  const [isTransferring, setIsTransferring] = useState(false);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);

  const isBusy =
    isLoadingAccounts ||
    isCreating ||
    isLoadingAccount ||
    isUpdating ||
    isDeleting ||
    isDepositing ||
    isWithdrawing ||
    isTransferring ||
    isLoadingHistory;

  const prettySelectedAccount = useMemo(() => {
    if (!selectedAccount) {
      return "";
    }
    return JSON.stringify(selectedAccount, null, 2);
  }, [selectedAccount]);

  useEffect(() => {
    if (!hasAccessToken) {
      return;
    }

    void loadAccounts();
  }, [hasAccessToken]);

  useEffect(() => {
    if (!selectedAccount) {
      return;
    }

    setTransferSourceAccountId((current) => (current.trim() ? current : selectedAccount.accountId));
  }, [selectedAccount]);

  useEffect(() => {
    if (!copiedAccountId) {
      return;
    }

    const resetTimer = window.setTimeout(() => {
      setCopiedAccountId(null);
    }, 1400);

    return () => {
      window.clearTimeout(resetTimer);
    };
  }, [copiedAccountId]);

  async function loadAccounts() {
    if (!hasAccessToken) {
      return;
    }

    setIsLoadingAccounts(true);
    try {
      const response = await listAccounts(accessToken, {
        accountType: accountTypeFilter || undefined,
        page: 1,
        size: 100,
      });
      setAccounts(response.items);

      if (selectedAccountId.trim()) {
        const matched = response.items.find((item) => item.accountId === selectedAccountId.trim());
        if (matched) {
          setSelectedAccount(matched);
          setUpdateNickname(matched.nickname ?? "");
        }
      }
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsLoadingAccounts(false);
    }
  }

  async function loadAccountDetail(accountId: string, refreshHistory = true) {
    if (!hasAccessToken) {
      return;
    }

    const trimmedAccountId = accountId.trim();
    if (!trimmedAccountId) {
      setError("Enter an account ID to load.");
      return;
    }

    setIsLoadingAccount(true);
    try {
      const account = await getAccount(accessToken, trimmedAccountId);
      setSelectedAccount(account);
      setSelectedAccountId(account.accountId);
      setUpdateNickname(account.nickname ?? "");
      setTransferSourceAccountId((current) => (current.trim() ? current : account.accountId));
      setStatus(`Loaded account ${account.accountId}.`);
      setError("");

      if (refreshHistory) {
        await loadHistory(account.accountId);
      }
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsLoadingAccount(false);
    }
  }

  async function loadHistory(accountId: string) {
    if (!hasAccessToken) {
      return;
    }

    const trimmedAccountId = accountId.trim();
    if (!trimmedAccountId) {
      return;
    }

    const resolvedPage = clampInteger(historyPage, 1, 1, 5000);
    const resolvedSize = clampInteger(historySize, 50, 1, 200);

    setIsLoadingHistory(true);
    try {
      const response = await getTransactionHistory(accessToken, trimmedAccountId, {
        from: toIsoDate(historyFrom),
        to: toIsoDate(historyTo),
        type: historyType || undefined,
        page: resolvedPage,
        size: resolvedSize,
      });
      setHistory(response);
      setHistoryPage(`${resolvedPage}`);
      setHistorySize(`${resolvedSize}`);
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsLoadingHistory(false);
    }
  }

  async function refreshAfterBalanceOperation(accountId: string) {
    await loadAccounts();
    await loadAccountDetail(accountId, false);
    await loadHistory(accountId);
  }

  async function handleCreateAccount(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    const accountName = createNickname.trim();
    if (!accountName) {
      setError("Account name is required before creating an account.");
      return;
    }

    setIsCreating(true);
    try {
      const created = await createAccount(accessToken, {
        accountType: createAccountType,
        nickname: accountName,
      });

      setStatus(`Created ${created.accountType.toLowerCase()} account ${created.accountId}.`);
      setCreateNickname("");
      setSelectedAccountId(created.accountId);
      await loadAccounts();
      await loadAccountDetail(created.accountId);
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsCreating(false);
    }
  }

  async function handleLoadAccount(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    await loadAccountDetail(selectedAccountId);
  }

  async function handleUpdateAccount(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedAccount) {
      setError("Load an account before updating.");
      return;
    }

    const nickname = updateNickname.trim();
    if (!nickname) {
      setError("Provide a nickname to update.");
      return;
    }

    setIsUpdating(true);
    try {
      const updated = await updateAccount(accessToken, selectedAccount.accountId, { nickname });
      setSelectedAccount(updated);
      setUpdateNickname(updated.nickname ?? "");
      setStatus(`Updated account ${updated.accountId}.`);
      await loadAccounts();
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsUpdating(false);
    }
  }

  async function handleDeleteAccount(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedAccount) {
      setError("Load an account before deleting.");
      return;
    }

    setIsDeleting(true);
    try {
      await deleteAccount(accessToken, selectedAccount.accountId, {
        idempotencyKey: createIdempotencyKey("delete"),
      });

      setStatus(`Deleted account ${selectedAccount.accountId}.`);
      setSelectedAccount(null);
      setSelectedAccountId("");
      setUpdateNickname("");
      setHistory(null);
      await loadAccounts();
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsDeleting(false);
    }
  }

  async function handleDeposit(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedAccount) {
      setError("Load an account before depositing.");
      return;
    }

    const amount = parsePositiveAmount(depositAmount);
    if (amount === null) {
      setError("Enter a valid positive deposit amount.");
      return;
    }

    setIsDepositing(true);
    try {
      const movement = await depositFunds(accessToken, selectedAccount.accountId, {
        amount,
        idempotencyKey: createIdempotencyKey("deposit"),
      });
      setStatus(`Posted deposit ${movement.movementId}.`);
      setDepositAmount("");
      await refreshAfterBalanceOperation(selectedAccount.accountId);
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsDepositing(false);
    }
  }

  async function handleWithdraw(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedAccount) {
      setError("Load an account before withdrawing.");
      return;
    }

    const amount = parsePositiveAmount(withdrawAmount);
    if (amount === null) {
      setError("Enter a valid positive withdrawal amount.");
      return;
    }

    setIsWithdrawing(true);
    try {
      const movement = await withdrawFunds(accessToken, selectedAccount.accountId, {
        amount,
        idempotencyKey: createIdempotencyKey("withdraw"),
      });
      setStatus(`Posted withdrawal ${movement.movementId}.`);
      setWithdrawAmount("");
      await refreshAfterBalanceOperation(selectedAccount.accountId);
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsWithdrawing(false);
    }
  }

  async function handleTransfer(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    const sourceAccountId = transferSourceAccountId.trim();
    const destinationAccountId = transferDestinationAccountId.trim();

    if (!sourceAccountId || !destinationAccountId) {
      setError("Provide both source and destination account IDs for transfer.");
      return;
    }

    if (sourceAccountId === destinationAccountId) {
      setError("Source and destination account IDs must be different.");
      return;
    }

    const amount = parsePositiveAmount(transferAmount);
    if (amount === null) {
      setError("Enter a valid positive transfer amount.");
      return;
    }

    setIsTransferring(true);
    try {
      const transfer = await transferFunds(accessToken, {
        sourceAccountId,
        destinationAccountId,
        amount,
        idempotencyKey: createIdempotencyKey("transfer"),
      });

      setStatus(`Posted transfer ${transfer.transferId}.`);
      setTransferAmount("");
      await loadAccounts();

      if (selectedAccountId.trim() === sourceAccountId || selectedAccountId.trim() === destinationAccountId) {
        await loadAccountDetail(selectedAccountId.trim(), true);
      }
    } catch (err) {
      handleAccountError(err);
    } finally {
      setIsTransferring(false);
    }
  }

  async function handleLoadHistory(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!selectedAccount) {
      setError("Load an account before requesting transaction history.");
      return;
    }

    await loadHistory(selectedAccount.accountId);
  }

  function handleAccountError(err: unknown) {
    const mapped = mapAccountApiError(err);
    setError(mapped.message);

    if (isSessionRecoveryRequired(mapped.code)) {
      signOut();
    }
  }

  async function handleCopyAccountId(accountId: string) {
    if (typeof navigator === "undefined" || !navigator.clipboard) {
      setError("Clipboard is not available. Please copy the account ID manually.");
      setCopiedAccountId(null);
      return;
    }

    try {
      await navigator.clipboard.writeText(accountId);
      setStatus(`Copied account ID ${accountId}.`);
      setError("");
      setCopiedAccountId(accountId);
    } catch {
      setError("Unable to copy account ID. Please try again.");
      setCopiedAccountId(null);
    }
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Accounts</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Account operations</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Open, maintain, and transact on authenticated accounts. Balances, transfers, and transaction history stay in one workspace.
        </p>
        {!hasAccessToken ? (
          <p className="mt-4 rounded-xl border border-amber-300/30 bg-amber-400/10 px-4 py-3 text-sm text-amber-100">
            Session token is missing. Sign out and sign in again before running account operations.
          </p>
        ) : null}
      </header>

      <div className="grid gap-6 xl:grid-cols-3">
        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Create account</h3>
          <p className="mt-2 text-sm text-slate-300">Open a new checking or savings account with a required account name.</p>

          <form onSubmit={handleCreateAccount} className="mt-4 grid gap-3 sm:grid-cols-2">
            <select
              aria-label="Create account type"
              value={createAccountType}
              onChange={(event) => setCreateAccountType(event.target.value as AccountType)}
              className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            >
              {accountTypeOptions.map((accountType) => (
                <option key={accountType} value={accountType}>
                  {accountType}
                </option>
              ))}
            </select>
            <input
              aria-label="Create account name"
              type="text"
              required
              value={createNickname}
              onChange={(event) => setCreateNickname(event.target.value)}
              placeholder="Account name"
              className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!hasAccessToken || isBusy}
              className="sm:col-span-2 inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
            >
              {isCreating ? "Creating..." : "Create account"}
            </button>
          </form>
        </article>

        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Account listing</h3>
          <p className="mt-2 text-sm text-slate-300">Filter and load existing accounts independently from account creation.</p>

          <div className="mt-4 flex flex-wrap items-center gap-2">
            <select
              aria-label="Filter account type"
              value={accountTypeFilter}
              onChange={(event) => setAccountTypeFilter(event.target.value as "" | AccountType)}
              className="rounded-full border border-white/20 bg-slate-950/70 px-3 py-2 text-xs font-semibold text-slate-100 focus:border-cyan-300 focus:outline-none"
            >
              <option value="">All account types</option>
              {accountTypeOptions.map((accountType) => (
                <option key={accountType} value={accountType}>
                  {accountType}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() => {
                void loadAccounts();
              }}
              disabled={!hasAccessToken || isBusy}
              className="inline-flex items-center justify-center rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoadingAccounts ? "Refreshing..." : "Refresh list"}
            </button>
          </div>

          <div className="mt-4 space-y-2">
            {accounts.length === 0 ? (
              <p className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
                No accounts loaded yet.
              </p>
            ) : (
              accounts.map((account) => (
                <article
                  key={account.accountId}
                  className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 transition hover:border-cyan-300/40 hover:bg-slate-900/70"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <p className="text-xs uppercase tracking-[0.14em] text-slate-400">{account.accountType}</p>
                      <p className="mt-1 break-all text-sm font-semibold text-white">{account.accountId}</p>
                      <p className="mt-1 text-xs text-slate-300">
                        {account.nickname || "Unnamed account"} • {account.currencyCode} {formatMoney(account.availableBalance)}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => {
                        void handleCopyAccountId(account.accountId);
                      }}
                      className={[
                        "shrink-0 rounded-full px-3 py-1.5 text-xs font-semibold transition",
                        copiedAccountId === account.accountId
                          ? "bg-emerald-300 text-slate-950"
                          : "bg-white/10 text-white hover:bg-white/20",
                      ].join(" ")}
                    >
                      {copiedAccountId === account.accountId ? "Copied" : "Copy ID"}
                    </button>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      void loadAccountDetail(account.accountId);
                    }}
                    className="mt-3 inline-flex items-center justify-center rounded-full bg-cyan-300 px-3 py-1.5 text-xs font-semibold text-slate-950 transition hover:bg-cyan-200"
                  >
                    Load account
                  </button>
                </article>
              ))
            )}
          </div>
        </article>

        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Selected account</h3>

          <form onSubmit={handleLoadAccount} className="mt-4 space-y-3">
            <input
              aria-label="Load account id"
              type="text"
              required
              value={selectedAccountId}
              onChange={(event) => setSelectedAccountId(event.target.value)}
              placeholder="Account ID"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!hasAccessToken || isBusy || !selectedAccountId.trim()}
              className="inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isLoadingAccount ? "Loading..." : "Load account"}
            </button>
          </form>

          {selectedAccount ? (
            <div className="mt-4 space-y-4">
              <div className="grid gap-3 rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200 sm:grid-cols-2">
                <div>
                  <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Account ID</p>
                  <p className="mt-1 break-all font-medium text-white">{selectedAccount.accountId}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Type</p>
                  <p className="mt-1 font-medium text-white">{selectedAccount.accountType}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Available balance</p>
                  <p className="mt-1 font-medium text-white">{selectedAccount.currencyCode} {formatMoney(selectedAccount.availableBalance)}</p>
                </div>
                <div>
                  <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Ledger balance</p>
                  <p className="mt-1 font-medium text-white">{selectedAccount.currencyCode} {formatMoney(selectedAccount.ledgerBalance)}</p>
                </div>
              </div>

              <form onSubmit={handleUpdateAccount} className="space-y-3 border-t border-white/10 pt-4">
                <input
                  aria-label="Update account nickname"
                  type="text"
                  value={updateNickname}
                  onChange={(event) => setUpdateNickname(event.target.value)}
                  placeholder="Nickname"
                  className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
                />
                <button
                  type="submit"
                  disabled={!hasAccessToken || isBusy || !selectedAccount}
                  className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
                >
                  {isUpdating ? "Updating..." : "Update nickname"}
                </button>
              </form>

              <form onSubmit={handleDeleteAccount} className="space-y-3 border-t border-white/10 pt-4">
                <button
                  type="submit"
                  disabled={!hasAccessToken || isBusy || !selectedAccount}
                  className="inline-flex w-full items-center justify-center rounded-xl bg-rose-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-rose-200 disabled:cursor-not-allowed disabled:bg-rose-200/70"
                >
                  {isDeleting ? "Deleting..." : "Delete account"}
                </button>
                <p className="text-xs text-slate-400">
                  Account deletion is blocked only when pending movement activity exists.
                </p>
              </form>
            </div>
          ) : (
            <p className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
              Load an account to edit, delete, transact, and view history.
            </p>
          )}
        </article>
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Movements</h3>
          <p className="mt-2 text-sm text-slate-300">Run deposits and withdrawals on the currently selected account.</p>

          <form onSubmit={handleDeposit} className="mt-4 space-y-3">
            <input
              aria-label="Deposit amount"
              type="number"
              step="0.01"
              min="0.01"
              value={depositAmount}
              onChange={(event) => setDepositAmount(event.target.value)}
              placeholder="Deposit amount"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!hasAccessToken || isBusy || !selectedAccount}
              className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
            >
              {isDepositing ? "Posting deposit..." : "Deposit"}
            </button>
          </form>

          <form onSubmit={handleWithdraw} className="mt-4 space-y-3 border-t border-white/10 pt-4">
            <input
              aria-label="Withdraw amount"
              type="number"
              step="0.01"
              min="0.01"
              value={withdrawAmount}
              onChange={(event) => setWithdrawAmount(event.target.value)}
              placeholder="Withdraw amount"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!hasAccessToken || isBusy || !selectedAccount}
              className="inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isWithdrawing ? "Posting withdrawal..." : "Withdraw"}
            </button>
          </form>
        </article>

        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Transfers</h3>
          <p className="mt-2 text-sm text-slate-300">Move funds between two different accounts.</p>

          <form onSubmit={handleTransfer} className="mt-4 space-y-3">
            <select
              aria-label="Transfer source account"
              value={transferSourceAccountId}
              onChange={(event) => setTransferSourceAccountId(event.target.value)}
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            >
              <option value="">Select source account</option>
              {accounts.map((account) => (
                <option key={account.accountId} value={account.accountId}>
                  {account.accountType} • {account.accountId}
                </option>
              ))}
            </select>
            <input
              aria-label="Transfer destination account"
              type="text"
              value={transferDestinationAccountId}
              onChange={(event) => setTransferDestinationAccountId(event.target.value)}
              placeholder="Destination account ID"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <input
              aria-label="Transfer amount"
              type="number"
              step="0.01"
              min="0.01"
              value={transferAmount}
              onChange={(event) => setTransferAmount(event.target.value)}
              placeholder="Transfer amount"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!hasAccessToken || isBusy}
              className="inline-flex w-full items-center justify-center rounded-xl bg-indigo-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-indigo-200 disabled:cursor-not-allowed disabled:bg-indigo-200/70"
            >
              {isTransferring ? "Posting transfer..." : "Transfer"}
            </button>
          </form>
        </article>
      </div>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <h3 className="text-xl font-semibold text-white">Transaction history</h3>

        <form onSubmit={handleLoadHistory} className="mt-4 grid gap-3 sm:grid-cols-2">
          <input
            aria-label="History from timestamp"
            type="datetime-local"
            value={historyFrom}
            onChange={(event) => setHistoryFrom(event.target.value)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <input
            aria-label="History to timestamp"
            type="datetime-local"
            value={historyTo}
            onChange={(event) => setHistoryTo(event.target.value)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <select
            aria-label="History movement type"
            value={historyType}
            onChange={(event) => setHistoryType(event.target.value as "" | MovementType)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          >
            <option value="">All movement types</option>
            {historyTypeOptions.map((movementType) => (
              <option key={movementType} value={movementType}>
                {movementType}
              </option>
            ))}
          </select>
          <div className="grid grid-cols-2 gap-3">
            <input
              aria-label="History page"
              type="number"
              min="1"
              value={historyPage}
              onChange={(event) => setHistoryPage(event.target.value)}
              placeholder="Page"
              className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <input
              aria-label="History page size"
              type="number"
              min="1"
              max="200"
              value={historySize}
              onChange={(event) => setHistorySize(event.target.value)}
              placeholder="Size"
              className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
          </div>
          <button
            type="submit"
            disabled={!hasAccessToken || isBusy || !selectedAccount}
            className="sm:col-span-2 inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
          >
            {isLoadingHistory ? "Loading history..." : "Load history"}
          </button>
        </form>

        {history && history.items.length > 0 ? (
          <div className="mt-4 overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
            <table className="min-w-full text-left text-xs text-slate-200">
              <thead className="border-b border-white/10 text-slate-400">
                <tr>
                  <th className="px-3 py-2 font-semibold">Type</th>
                  <th className="px-3 py-2 font-semibold">Amount</th>
                  <th className="px-3 py-2 font-semibold">Balance</th>
                  <th className="px-3 py-2 font-semibold">Created</th>
                </tr>
              </thead>
              <tbody>
                {history.items.map((item) => (
                  <tr key={item.movementId} className="border-b border-white/5">
                    <td className="px-3 py-2">{item.movementType}</td>
                    <td className="px-3 py-2">{formatMoney(item.amount)}</td>
                    <td className="px-3 py-2">{formatMoney(item.balanceAfter)}</td>
                    <td className="px-3 py-2">{formatTimestamp(item.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
            {history ? "No transactions matched the selected filters." : "History results appear here after loading an account."}
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

      {prettySelectedAccount ? (
        <details className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <summary className="cursor-pointer text-sm font-semibold text-cyan-200">View selected account payload</summary>
          <pre className="mt-3 overflow-x-auto text-xs leading-6 text-slate-200">{prettySelectedAccount}</pre>
        </details>
      ) : null}
    </section>
  );
}

function normalizeOptional(value: string): string | undefined {
  const trimmed = value.trim();
  return trimmed ? trimmed : undefined;
}

function parsePositiveAmount(value: string): number | null {
  const parsed = parseAmountValue(value);
  if (parsed === null || parsed <= 0) {
    return null;
  }
  return Number(parsed.toFixed(2));
}

function parseAmountValue(value: string | undefined): number | null {
  if (!value) {
    return null;
  }

  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed)) {
    return null;
  }

  return parsed;
}

function formatMoney(value: string): string {
  const parsed = parseAmountValue(value);
  if (parsed === null) {
    return "0.00";
  }
  return parsed.toFixed(2);
}

function toIsoDate(value: string): string | undefined {
  const trimmed = value.trim();
  if (!trimmed) {
    return undefined;
  }

  const parsed = new Date(trimmed);
  if (Number.isNaN(parsed.getTime())) {
    return undefined;
  }

  return parsed.toISOString();
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

function createIdempotencyKey(prefix: string): string {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return `${prefix}-${crypto.randomUUID()}`;
  }
  return `${prefix}-${Date.now()}`;
}
