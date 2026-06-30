import { FormEvent, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import { getMonthlyStatement, generateMonthlyStatement } from "../api/statements";
import { isSessionRecoveryRequired, mapStatementApiError } from "../api/errorMapper";

export function StatementsPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [accountId, setAccountId] = useState("");
  const [month, setMonth] = useState("");

  const [statement, setStatement] = useState<Awaited<ReturnType<typeof getMonthlyStatement>> | null>(null);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [isGenerating, setIsGenerating] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  type StatementAction = "generate" | "retrieve";

  async function handleGenerate(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!accountId.trim() || !month.trim()) {
      setError("Provide both account ID and month before generating statement.");
      return;
    }

    setIsGenerating(true);
    try {
      const generated = await generateMonthlyStatement(accessToken, accountId.trim(), month.trim());
      setStatement(generated);
      setStatus(`Generated statement for ${generated.accountId} (${generated.month}).`);
    } catch (err) {
      handleStatementError(err, "generate");
    } finally {
      setIsGenerating(false);
    }
  }

  async function handleLoad(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!accountId.trim() || !month.trim()) {
      setError("Provide both account ID and month before retrieving statement.");
      return;
    }

    setIsLoading(true);
    try {
      const loaded = await getMonthlyStatement(accessToken, accountId.trim(), month.trim());
      setStatement(loaded);
      setStatus(`Loaded statement for ${loaded.accountId} (${loaded.month}).`);
    } catch (err) {
      handleStatementError(err, "retrieve");
    } finally {
      setIsLoading(false);
    }
  }

  function handleStatementError(err: unknown, action: StatementAction) {
    const mapped = mapStatementApiError(err);
    const actionLabel = action === "generate" ? "generate" : "retrieve";
    setError(`Unable to ${actionLabel} monthly statement: ${mapped.message}`);

    if (isSessionRecoveryRequired(mapped.code)) {
      signOut();
    }
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Statements</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Monthly statements</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Generate and retrieve monthly statements.
        </p>
      </header>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <h3 className="text-xl font-semibold text-white">Generate or retrieve</h3>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <input
            type="text"
            value={accountId}
            onChange={(event) => setAccountId(event.target.value)}
            placeholder="Account ID"
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <input
            type="month"
            value={month}
            onChange={(event) => setMonth(event.target.value)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <form onSubmit={handleGenerate}>
            <button
              type="submit"
              disabled={!hasAccessToken || isGenerating || isLoading}
              className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
            >
              {isGenerating ? "Generating..." : "Generate statement"}
            </button>
          </form>

          <form onSubmit={handleLoad}>
            <button
              type="submit"
              disabled={!hasAccessToken || isGenerating || isLoading}
              className="inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isLoading ? "Loading..." : "Retrieve statement"}
            </button>
          </form>
        </div>

        {statement ? (
          <div className="mt-6 space-y-4">
            <div className="grid gap-3 rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200 sm:grid-cols-2">
              <p>
                <span className="text-slate-400">Account:</span> {statement.accountId}
              </p>
              <p>
                <span className="text-slate-400">Month:</span> {statement.month}
              </p>
              <p>
                <span className="text-slate-400">Opening balance:</span> ${formatMoney(statement.openingBalance)}
              </p>
              <p>
                <span className="text-slate-400">Closing balance:</span> ${formatMoney(statement.closingBalance)}
              </p>
              <p>
                <span className="text-slate-400">Total debits:</span> ${formatMoney(statement.totalDebits)}
              </p>
              <p>
                <span className="text-slate-400">Total credits:</span> ${formatMoney(statement.totalCredits)}
              </p>
            </div>

            {statement.lineItems.length > 0 ? (
              <div className="overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
                <table className="min-w-full text-left text-xs text-slate-200">
                  <thead className="border-b border-white/10 text-slate-400">
                    <tr>
                      <th className="px-3 py-2 font-semibold">Posted</th>
                      <th className="px-3 py-2 font-semibold">Entry</th>
                      <th className="px-3 py-2 font-semibold">Amount</th>
                      <th className="px-3 py-2 font-semibold">Balance</th>
                      <th className="px-3 py-2 font-semibold">Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statement.lineItems.map((item) => (
                      <tr key={`${item.transactionId}-${item.postedAt}`} className="border-b border-white/5">
                        <td className="px-3 py-2">{formatTimestamp(item.postedAt)}</td>
                        <td className="px-3 py-2">{item.entryType}</td>
                        <td className="px-3 py-2">${formatMoney(item.amount)}</td>
                        <td className="px-3 py-2">${formatMoney(item.balanceAfter)}</td>
                        <td className="px-3 py-2 text-slate-300">{item.description}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
                This month has no posted activity. Totals remain valid with zeroed line items.
              </p>
            )}
          </div>
        ) : null}
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

function formatTimestamp(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function formatMoney(value: string): string {
  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed)) {
    return "0.00";
  }
  return parsed.toFixed(2);
}
