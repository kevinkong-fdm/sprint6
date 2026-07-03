import { FormEvent, useMemo, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import { getSpendingInsights } from "../api/insights";
import { isSessionRecoveryRequired, mapInsightsApiError } from "../api/errorMapper";

type InsightsResult = Awaited<ReturnType<typeof getSpendingInsights>>;

type CategoryInsight = {
  category: string;
  currentTotal: number;
  previousTotal: number | null;
  deltaAmount: number | null;
  deltaPercent: number | null;
  sharePercent: number;
};

type DerivedInsights = {
  currentTotal: number;
  previousTotal: number | null;
  deltaAmount: number | null;
  deltaPercent: number | null;
  averageDaily: number;
  projectedThirtyDay: number;
  activeDays: number;
  topCategory: CategoryInsight | null;
  fastestRiser: CategoryInsight | null;
  concentrationTop3: number | null;
  momentumLabel: string;
  categories: CategoryInsight[];
};

export function InsightsPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [periodStart, setPeriodStart] = useState("");
  const [periodEnd, setPeriodEnd] = useState("");
  const [accountId, setAccountId] = useState("");

  const [result, setResult] = useState<InsightsResult | null>(null);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const insights = useMemo(() => {
    if (!result) {
      return null;
    }
    return deriveInsights(result);
  }, [result]);

  async function loadInsights(periodStartValue: string, periodEndValue: string) {
    setError("");

    if (!hasAccessToken) {
      setStatus("");
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!periodStartValue.trim() || !periodEndValue.trim()) {
      setStatus("");
      setError("Provide both period start and end dates for insights.");
      return;
    }

    setStatus("Loading insights...");
    setIsLoading(true);
    try {
      const insightsResponse = await getSpendingInsights(accessToken, {
        periodStart: periodStartValue,
        periodEnd: periodEndValue,
        accountId: accountId.trim() || undefined,
      });

      setResult(insightsResponse);
      setStatus("Loaded spending insights successfully.");
    } catch (err) {
      setStatus("");
      const mapped = mapInsightsApiError(err);
      setError(mapped.message);

      if (isSessionRecoveryRequired(mapped.code)) {
        signOut();
      }
    } finally {
      setIsLoading(false);
    }
  }

  function applyRollingPreset(days: number) {
    const end = new Date();
    const start = new Date(end);
    start.setDate(end.getDate() - (days - 1));

    const startValue = toInputDate(start);
    const endValue = toInputDate(end);

    setPeriodStart(startValue);
    setPeriodEnd(endValue);
    void loadInsights(startValue, endValue);
  }

  function applyMonthToDatePreset() {
    const end = new Date();
    const start = new Date(end.getFullYear(), end.getMonth(), 1);

    const startValue = toInputDate(start);
    const endValue = toInputDate(end);

    setPeriodStart(startValue);
    setPeriodEnd(endValue);
    void loadInsights(startValue, endValue);
  }

  async function handleLoadInsights(event: FormEvent) {
    event.preventDefault();
    await loadInsights(periodStart, periodEnd);
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Insights</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Spending intelligence</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Discover where money flows, what is accelerating, and how current habits may project into the next month.
        </p>
      </header>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <h3 className="text-xl font-semibold text-white">Insight window</h3>

        <div className="mt-4 flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => applyRollingPreset(7)}
            disabled={!hasAccessToken || isLoading}
            className="rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Last 7 days
          </button>
          <button
            type="button"
            onClick={() => applyRollingPreset(30)}
            disabled={!hasAccessToken || isLoading}
            className="rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Last 30 days
          </button>
          <button
            type="button"
            onClick={() => applyRollingPreset(90)}
            disabled={!hasAccessToken || isLoading}
            className="rounded-full bg-white/10 px-3 py-2 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Last 90 days
          </button>
          <button
            type="button"
            onClick={applyMonthToDatePreset}
            disabled={!hasAccessToken || isLoading}
            className="rounded-full bg-cyan-300 px-3 py-2 text-xs font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
          >
            Month to date
          </button>
        </div>

        <form onSubmit={handleLoadInsights} className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
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

        {result && insights ? (
          <div className="mt-6 space-y-4">
            <div className="rounded-xl border border-cyan-300/30 bg-cyan-400/10 p-4 text-sm text-cyan-100">
              <p>
                Window {result.periodStart} to {result.periodEnd} ({insights.activeDays} day{insights.activeDays === 1 ? "" : "s"})
              </p>
              <p className="mt-1 text-cyan-50">
                {buildNarrative(insights)}
              </p>
            </div>

            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
              <InsightCard
                label="Spend This Period"
                value={formatCurrency(insights.currentTotal)}
                tone="cyan"
              />
              <InsightCard
                label="Average Per Day"
                value={formatCurrency(insights.averageDaily)}
                tone="slate"
              />
              <InsightCard
                label="30-Day Projection"
                value={formatCurrency(insights.projectedThirtyDay)}
                tone="indigo"
              />
              <InsightCard
                label="Momentum"
                value={insights.momentumLabel}
                subValue={formatPercent(insights.deltaPercent)}
                tone="emerald"
              />
            </div>

            <div className="grid gap-3 lg:grid-cols-3">
              <div className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200">
                <h4 className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">Top Category</h4>
                <p className="mt-2 text-base font-semibold text-white">
                  {insights.topCategory ? prettyCategory(insights.topCategory.category) : "No category data"}
                </p>
                <p className="mt-1 text-sm text-slate-300">
                  {insights.topCategory
                    ? `${formatCurrency(insights.topCategory.currentTotal)} (${insights.topCategory.sharePercent.toFixed(1)}% of spend)`
                    : "No current spending to analyze."}
                </p>
              </div>

              <div className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200">
                <h4 className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">Fastest Rising</h4>
                <p className="mt-2 text-base font-semibold text-white">
                  {insights.fastestRiser ? prettyCategory(insights.fastestRiser.category) : "Not enough history"}
                </p>
                <p className="mt-1 text-sm text-slate-300">
                  {insights.fastestRiser
                    ? `${formatSignedCurrency(insights.fastestRiser.deltaAmount)} (${formatPercent(insights.fastestRiser.deltaPercent)})`
                    : "Run a wider date range to surface category movement."}
                </p>
              </div>

              <div className="rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200">
                <h4 className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">Spend Concentration</h4>
                <p className="mt-2 text-base font-semibold text-white">
                  {insights.concentrationTop3 === null ? "-" : `${insights.concentrationTop3.toFixed(1)}%`}
                </p>
                <p className="mt-1 text-sm text-slate-300">
                  {insights.concentrationTop3 === null
                    ? "No spend to evaluate."
                    : "Share of spending captured by your top 3 categories."}
                </p>
              </div>
            </div>

            {insights.categories.length > 0 ? (
              <div className="overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
                <table className="min-w-full text-left text-xs text-slate-200">
                  <thead className="border-b border-white/10 text-slate-400">
                    <tr>
                      <th className="px-3 py-2 font-semibold">Category</th>
                      <th className="px-3 py-2 font-semibold">Current</th>
                      <th className="px-3 py-2 font-semibold">Share</th>
                      <th className="px-3 py-2 font-semibold">Change</th>
                    </tr>
                  </thead>
                  <tbody>
                    {insights.categories.map((category) => (
                      <tr key={category.category} className="border-b border-white/5">
                        <td className="px-3 py-2">{prettyCategory(category.category)}</td>
                        <td className="px-3 py-2">{formatCurrency(category.currentTotal)}</td>
                        <td className="px-3 py-2">
                          <div className="flex items-center gap-2">
                            <span>{category.sharePercent.toFixed(1)}%</span>
                            <span className="h-1.5 w-20 overflow-hidden rounded-full bg-white/10">
                              <span
                                className="block h-full rounded-full bg-cyan-300"
                                style={{ width: `${Math.min(100, Math.max(0, category.sharePercent))}%` }}
                              />
                            </span>
                          </div>
                        </td>
                        <td className="px-3 py-2">{formatSignedCurrency(category.deltaAmount)}</td>
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

type InsightCardProps = {
  label: string;
  value: string;
  subValue?: string;
  tone: "cyan" | "slate" | "indigo" | "emerald";
};

function InsightCard({ label, value, subValue, tone }: InsightCardProps) {
  const toneClass = {
    cyan: "border-cyan-300/30 bg-cyan-400/10",
    slate: "border-white/15 bg-white/5",
    indigo: "border-indigo-300/30 bg-indigo-400/10",
    emerald: "border-emerald-300/30 bg-emerald-400/10",
  }[tone];

  return (
    <div className={`rounded-xl border p-4 ${toneClass}`}>
      <p className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-300">{label}</p>
      <p className="mt-2 text-lg font-semibold text-white">{value}</p>
      {subValue ? <p className="mt-1 text-xs text-slate-300">{subValue}</p> : null}
    </div>
  );
}

function deriveInsights(result: InsightsResult): DerivedInsights {
  const currentTotal = parseMoney(result.totals.currentTotal) ?? 0;
  const previousTotal = parseMoney(result.totals.previousTotal);
  const deltaAmount = parseMoney(result.totals.deltaAmount) ??
    (previousTotal === null ? null : currentTotal - previousTotal);

  const deltaPercent =
    result.totals.deltaPercent ??
    (previousTotal === null || previousTotal === 0 || deltaAmount === null
      ? null
      : (deltaAmount / previousTotal) * 100);

  const activeDays = daysInclusive(result.periodStart, result.periodEnd);
  const averageDaily = activeDays > 0 ? currentTotal / activeDays : currentTotal;
  const projectedThirtyDay = averageDaily * 30;

  const categories = result.categories
    .map((category): CategoryInsight => {
      const current = parseMoney(category.currentTotal) ?? 0;
      const previous = parseMoney(category.previousTotal);
      const delta = parseMoney(category.deltaAmount) ??
        (previous === null ? null : current - previous);
      const pct =
        category.deltaPercent ??
        (previous === null || previous === 0 || delta === null ? null : (delta / previous) * 100);

      return {
        category: category.category,
        currentTotal: current,
        previousTotal: previous,
        deltaAmount: delta,
        deltaPercent: pct,
        sharePercent: currentTotal <= 0 ? 0 : (current / currentTotal) * 100,
      };
    })
    .sort((left, right) => right.currentTotal - left.currentTotal);

  const topCategory = categories[0] ?? null;

  const fastestRiser = categories
    .filter((category) => category.deltaAmount !== null && category.deltaAmount > 0)
    .sort((left, right) => (right.deltaAmount ?? 0) - (left.deltaAmount ?? 0))[0] ?? null;

  const top3 = categories.slice(0, 3).reduce((sum, category) => sum + category.currentTotal, 0);
  const concentrationTop3 = currentTotal > 0 ? (top3 / currentTotal) * 100 : null;

  return {
    currentTotal,
    previousTotal,
    deltaAmount,
    deltaPercent,
    averageDaily,
    projectedThirtyDay,
    activeDays,
    topCategory,
    fastestRiser,
    concentrationTop3,
    momentumLabel: momentumLabel(deltaPercent),
    categories,
  };
}

function buildNarrative(insights: DerivedInsights): string {
  const change = formatSignedCurrency(insights.deltaAmount);
  const pace = formatCurrency(insights.averageDaily);
  const topCategory = insights.topCategory ? prettyCategory(insights.topCategory.category) : "uncategorized spending";

  return `Spend pace is ${pace} per day, with ${topCategory} currently leading. Period change: ${change}.`;
}

function momentumLabel(deltaPercent: number | null): string {
  if (deltaPercent === null) {
    return "Baseline established";
  }
  if (deltaPercent >= 10) {
    return "Rising quickly";
  }
  if (deltaPercent >= 2) {
    return "Rising gradually";
  }
  if (deltaPercent <= -10) {
    return "Cooling quickly";
  }
  if (deltaPercent <= -2) {
    return "Cooling gradually";
  }
  return "Stable";
}

function parseMoney(value: string | undefined): number | null {
  if (!value) {
    return null;
  }
  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed)) {
    return null;
  }
  return parsed;
}

function formatCurrency(value: number | null): string {
  if (value === null) {
    return "-";
  }
  return `$${value.toFixed(2)}`;
}

function formatSignedCurrency(value: number | null): string {
  if (value === null) {
    return "-";
  }
  if (value === 0) {
    return "$0.00";
  }
  const sign = value > 0 ? "+" : "-";
  return `${sign}$${Math.abs(value).toFixed(2)}`;
}

function formatPercent(value: number | null): string {
  if (value === null) {
    return "-";
  }
  const sign = value > 0 ? "+" : "";
  return `${sign}${value.toFixed(1)}%`;
}

function prettyCategory(value: string): string {
  return value
    .split("_")
    .map((segment) => segment.charAt(0).toUpperCase() + segment.slice(1).toLowerCase())
    .join(" ");
}

function daysInclusive(start: string, end: string): number {
  const startDate = new Date(`${start}T00:00:00`);
  const endDate = new Date(`${end}T00:00:00`);
  if (Number.isNaN(startDate.getTime()) || Number.isNaN(endDate.getTime())) {
    return 1;
  }

  const diffMs = endDate.getTime() - startDate.getTime();
  if (diffMs < 0) {
    return 1;
  }

  return Math.floor(diffMs / (24 * 60 * 60 * 1000)) + 1;
}

function toInputDate(value: Date): string {
  const year = value.getFullYear();
  const month = `${value.getMonth() + 1}`.padStart(2, "0");
  const day = `${value.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}
