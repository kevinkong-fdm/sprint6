import { FormEvent, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import { getSpendingInsights } from "../api/insights";
import { isSessionRecoveryRequired, mapInsightsApiError } from "../api/errorMapper";

export function InsightsPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [periodStart, setPeriodStart] = useState("");
  const [periodEnd, setPeriodEnd] = useState("");
  const [comparisonMode, setComparisonMode] = useState<"PREVIOUS_PERIOD" | "NONE">("PREVIOUS_PERIOD");
  const [accountId, setAccountId] = useState("");

  const [result, setResult] = useState<Awaited<ReturnType<typeof getSpendingInsights>> | null>(null);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleLoadInsights(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!periodStart.trim() || !periodEnd.trim()) {
      setError("Provide both period start and end dates for insights.");
      return;
    }

    setIsLoading(true);
    try {
      const insights = await getSpendingInsights(accessToken, {
        periodStart,
        periodEnd,
        comparisonMode,
        accountId: accountId.trim() || undefined,
      });

      setResult(insights);
      if (insights.insufficientData) {
        setStatus("Loaded insights with low-data indicator: insufficient history for complete comparison.");
      } else {
        setStatus("Loaded spending insights successfully.");
      }
    } catch (err) {
      const mapped = mapInsightsApiError(err);
      setError(mapped.message);

      if (isSessionRecoveryRequired(mapped.code)) {
        signOut();
      }
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Insights</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Spending insights</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Analyze categorized spending across selected periods.
        </p>
      </header>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <h3 className="text-xl font-semibold text-white">Request insights</h3>
        <form onSubmit={handleLoadInsights} className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          <input
            type="date"
            value={periodStart}
            onChange={(event) => setPeriodStart(event.target.value)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <input
            type="date"
            value={periodEnd}
            onChange={(event) => setPeriodEnd(event.target.value)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <select
            value={comparisonMode}
            onChange={(event) => setComparisonMode(event.target.value as "PREVIOUS_PERIOD" | "NONE")}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          >
            <option value="PREVIOUS_PERIOD">PREVIOUS_PERIOD</option>
            <option value="NONE">NONE</option>
          </select>
          <input
            type="text"
            value={accountId}
            onChange={(event) => setAccountId(event.target.value)}
            placeholder="Optional account ID"
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <button
            type="submit"
            disabled={!hasAccessToken || isLoading}
            className="inline-flex items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
          >
            {isLoading ? "Loading..." : "Load insights"}
          </button>
        </form>

        {result ? (
          <div className="mt-6 space-y-4">
            <div className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200">
              <p>
                <span className="text-slate-400">Period:</span> {result.periodStart} to {result.periodEnd}
              </p>
              <p className="mt-1">
                <span className="text-slate-400">Comparison mode:</span> {result.comparisonMode}
              </p>
              <p className="mt-1">
                <span className="text-slate-400">Insufficient data:</span> {result.insufficientData ? "Yes" : "No"}
              </p>
              <p className="mt-1">
                <span className="text-slate-400">Insufficiency reason:</span> {result.insufficiencyReason}
              </p>
            </div>

            <div className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200">
              <h4 className="text-sm font-semibold text-white">Totals</h4>
              <p className="mt-2">
                <span className="text-slate-400">Current:</span> ${formatMoney(result.totals.currentTotal)}
              </p>
              <p className="mt-1">
                <span className="text-slate-400">Previous:</span> {result.totals.previousTotal ? `$${formatMoney(result.totals.previousTotal)}` : "-"}
              </p>
              <p className="mt-1">
                <span className="text-slate-400">Delta:</span> {result.totals.deltaAmount ? `$${formatMoney(result.totals.deltaAmount)}` : "-"}
              </p>
              <p className="mt-1">
                <span className="text-slate-400">Delta %:</span> {result.totals.deltaPercent ?? "-"}
              </p>
            </div>

            {result.categories.length > 0 ? (
              <div className="overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
                <table className="min-w-full text-left text-xs text-slate-200">
                  <thead className="border-b border-white/10 text-slate-400">
                    <tr>
                      <th className="px-3 py-2 font-semibold">Category</th>
                      <th className="px-3 py-2 font-semibold">Current</th>
                      <th className="px-3 py-2 font-semibold">Previous</th>
                      <th className="px-3 py-2 font-semibold">Delta</th>
                      <th className="px-3 py-2 font-semibold">Delta %</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.categories.map((category) => (
                      <tr key={category.category} className="border-b border-white/5">
                        <td className="px-3 py-2">{category.category}</td>
                        <td className="px-3 py-2">${formatMoney(category.currentTotal)}</td>
                        <td className="px-3 py-2">{category.previousTotal ? `$${formatMoney(category.previousTotal)}` : "-"}</td>
                        <td className="px-3 py-2">{category.deltaAmount ? `$${formatMoney(category.deltaAmount)}` : "-"}</td>
                        <td className="px-3 py-2">{category.deltaPercent ?? "-"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
                No category breakdown available for the selected range.
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

function formatMoney(value: string): string {
  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed)) {
    return "0.00";
  }
  return parsed.toFixed(2);
}
