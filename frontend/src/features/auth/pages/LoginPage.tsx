import { FormEvent, useEffect, useState } from "react";
import { Link, useInRouterContext, useLocation, useNavigate } from "react-router-dom";
import { login } from "../api/login";
import { mapApiError } from "../api/errorMapper";
import { useAuthSession } from "../session/AuthSessionContext";

export function LoginPage() {
  const hasRouterContext = useInRouterContext();
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn, isAuthenticated } = useAuthSession();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }

    const redirectState = location.state as { from?: string } | null;
    navigate(redirectState?.from ?? "/customers", { replace: true });
  }, [isAuthenticated, location.state, navigate]);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const response = await login({ email, password });
      signIn({
        ...response,
        email,
      });
      setMessage(`Authenticated. Access token expires in ${response.accessTokenExpiresInSeconds} seconds.`);

      const redirectState = location.state as { from?: string } | null;
      navigate(redirectState?.from ?? "/customers", { replace: true });
    } catch (err) {
      const mapped = mapApiError(err);
      setError(mapped.message);
    }
  }

  return (
    <section className="mx-auto w-full max-w-2xl animate-fade-up rounded-3xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl sm:p-8">
      <div className="mb-6">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Access</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Sign in</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Continue to your account with secure authentication and session protection.
        </p>
      </div>

      <form onSubmit={onSubmit} className="space-y-4">
        <label className="block text-sm font-medium text-slate-200">
          <span>Email</span>
          <input
            aria-label="Login email"
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
            aria-label="Login password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            autoComplete="current-password"
            className="mt-2 w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            placeholder="Enter your password"
          />
        </label>

        <button
          type="submit"
          className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 focus:outline-none focus:ring-2 focus:ring-cyan-400/60"
        >
          Login
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
            <Link to="/register" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Need an account?
            </Link>
            <Link
              to="/password-reset"
              className="font-semibold text-cyan-300 transition hover:text-cyan-200"
            >
              Forgot password?
            </Link>
          </>
        ) : (
          <>
            <a href="/register" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Need an account?
            </a>
            <a href="/password-reset" className="font-semibold text-cyan-300 transition hover:text-cyan-200">
              Forgot password?
            </a>
          </>
        )}
      </div>
    </section>
  );
}
