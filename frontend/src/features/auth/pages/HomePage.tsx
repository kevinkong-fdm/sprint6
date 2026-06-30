import { Link } from "react-router-dom";
import { useAuthSession } from "../session/AuthSessionContext";

const guestActions = [
  {
    title: "Sign in",
    description: "Use your credentials to access active sessions and account controls.",
    to: "/login",
    cta: "Open login",
  },
  {
    title: "Register",
    description: "Create a new user profile with strong password validation and safe defaults.",
    to: "/register",
    cta: "Start registration",
  },
  {
    title: "Reset password",
    description: "Trigger recovery requests through the secure non-disclosing reset flow.",
    to: "/password-reset",
    cta: "Request reset",
  },
];

export function HomePage() {
  const { isAuthenticated } = useAuthSession();

  const visibleActions = isAuthenticated
    ? [
        {
          title: "Accounts",
          description: "Open the account workspace to create accounts, post deposits and withdrawals, transfer funds, and inspect transaction history.",
          to: "/accounts",
          cta: "Open accounts",
        },
        {
          title: "Customers",
          description: "Open the authenticated customer workspace and run create, retrieve, update, and delete operations.",
          to: "/customers",
          cta: "Open customers",
        },
        {
          title: "Standing orders",
          description: "Create recurring internal transfers, manage lifecycle states, and trigger execution attempts from one operational console.",
          to: "/standing-orders",
          cta: "Open standing orders",
        },
        {
          title: "Notifications",
          description: "Inspect standing-order lifecycle and execution notifications with event and dispatch status filtering.",
          to: "/notifications",
          cta: "Open notifications",
        },
        {
          title: "Statements",
          description: "Generate or retrieve monthly statements with deterministic totals, line items, and no-activity month behavior.",
          to: "/statements",
          cta: "Open statements",
        },
        {
          title: "Insights",
          description: "Load spending insight snapshots with category deltas and low-data indicators when historical data is limited.",
          to: "/insights",
          cta: "Open insights",
        },
      ]
    : guestActions;

  return (
    <section className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {visibleActions.map((action) => (
          <article
            key={action.to}
            className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl"
          >
            <h3 className="text-xl font-semibold text-white">{action.title}</h3>
            <p className="mt-2 text-sm leading-6 text-slate-300">{action.description}</p>
            <Link
              to={action.to}
              className="mt-4 inline-flex items-center rounded-full bg-cyan-300 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200"
            >
              {action.cta}
            </Link>
          </article>
        ))}
      </div>
    </section>
  );
}
