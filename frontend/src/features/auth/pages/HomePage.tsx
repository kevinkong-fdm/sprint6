import { Link } from "react-router-dom";

const actions = [
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
  return (
    <section className="space-y-6">
      <article className="animate-fade-up rounded-3xl border border-white/15 bg-gradient-to-br from-slate-900/80 via-slate-800/60 to-cyan-900/30 p-6 shadow-soft backdrop-blur-xl sm:p-8">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Overview</p>
        <h2 className="mt-2 text-3xl font-semibold text-white sm:text-4xl">Secure account access workflows</h2>
        <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-200/85 sm:text-base">
          Choose a workflow below to move through authentication tasks quickly. Every page is connected
          through navigation so operations can be completed without manually editing URL paths.
        </p>
      </article>

      <div className="grid gap-4 md:grid-cols-3">
        {actions.map((action) => (
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
