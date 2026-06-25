import { FormEvent, useState } from "react";
import { Link, useInRouterContext } from "react-router-dom";
import { mapApiError } from "../api/errorMapper";
import { requestPasswordReset } from "../api/passwordResetRequest";

export function PasswordResetRequestForm() {
  const hasRouterContext = useInRouterContext();
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setMessage("");
    setError("");
    setIsSubmitting(true);
    try {
      const response = await requestPasswordReset({ email });
      setMessage(response.message);
    } catch (err) {
      const mapped = mapApiError(err);
      setError(mapped.message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="mx-auto w-full max-w-2xl animate-fade-up rounded-3xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl sm:p-8">
      <div className="mb-6">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Recovery</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Password reset</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Submit your email to request a secure password reset workflow.
        </p>
      </div>

      <form onSubmit={onSubmit} className="space-y-4">
        <label className="block text-sm font-medium text-slate-200">
          <span>Email</span>
          <input
            aria-label="Reset email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            autoComplete="email"
            className="mt-2 w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            placeholder="you@domain.com"
          />
        </label>

        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 focus:outline-none focus:ring-2 focus:ring-cyan-400/60 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
        >
          {isSubmitting ? "Requesting..." : "Request reset"}
        </button>
      </form>

      {message ? (
        <p role="status" className="mt-4 rounded-xl border border-emerald-300/30 bg-emerald-400/10 px-4 py-3 text-sm text-emerald-100">
          {message}
        </p>
      ) : null}

      {error ? (
        <p role="alert" className="mt-4 rounded-xl border border-rose-300/30 bg-rose-400/10 px-4 py-3 text-sm text-rose-100">
          {error}
        </p>
      ) : null}

      <div className="mt-6 flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-slate-300">
        {hasRouterContext ? (
          <>
            <Link to="/login" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Back to login
            </Link>
            <Link to="/register" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Create account
            </Link>
          </>
        ) : (
          <>
            <a href="/login" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Back to login
            </a>
            <a href="/register" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Create account
            </a>
          </>
        )}
      </div>
    </section>
  );
}
