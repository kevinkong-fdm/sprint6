import { FormEvent, useState } from "react";
import { Link, useInRouterContext } from "react-router-dom";
import { registerAccount } from "../api/register";
import { mapApiError } from "../api/errorMapper";

export function RegistrationForm() {
  const hasRouterContext = useInRouterContext();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const response = await registerAccount({ email, password });
      setMessage(`Registered ${response.email}`);
    } catch (err) {
      const mapped = mapApiError(err);
      setError(mapped.message);
    }
  }

  return (
    <section className="mx-auto w-full max-w-2xl animate-fade-up rounded-3xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl sm:p-8">
      <div className="mb-6">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Onboarding</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Create account</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Register a new user with validated credentials and protected account policies.
        </p>
      </div>

      <form onSubmit={onSubmit} className="space-y-4">
        <label className="block text-sm font-medium text-slate-200">
          <span>Email</span>
          <input
            aria-label="Email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            autoComplete="email"
            className="mt-2 w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            placeholder="you@domain.com"
          />
        </label>

        <label className="block text-sm font-medium text-slate-200">
          <span>Password</span>
          <input
            aria-label="Password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            autoComplete="new-password"
            className="mt-2 w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            placeholder="Use at least 12 characters"
          />
        </label>

        <button
          type="submit"
          className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 focus:outline-none focus:ring-2 focus:ring-cyan-400/60"
        >
          Register
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
              Already registered?
            </Link>
            <Link
              to="/password-reset"
              className="font-semibold text-cyan-300 transition hover:text-cyan-200"
            >
              Need recovery?
            </Link>
          </>
        ) : (
          <>
            <a href="/login" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Already registered?
            </a>
            <a href="/password-reset" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Need recovery?
            </a>
          </>
        )}
      </div>
    </section>
  );
}
