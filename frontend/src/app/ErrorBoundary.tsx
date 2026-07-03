import React from "react";

type ErrorBoundaryState = {
  hasError: boolean;
};

export class ErrorBoundary extends React.Component<
  React.PropsWithChildren,
  ErrorBoundaryState
> {
  state: ErrorBoundaryState = {
    hasError: false,
  };

  static getDerivedStateFromError(): ErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: unknown) {
    console.error("Unhandled UI error", error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="mx-auto flex min-h-screen w-full max-w-2xl items-center px-4">
          <section className="w-full rounded-3xl border border-rose-300/30 bg-slate-900/70 p-8 text-center shadow-soft backdrop-blur-xl">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-rose-200/80">Application error</p>
            <h1 className="mt-2 text-3xl font-semibold text-white">Something went wrong</h1>
            <p className="mt-3 text-sm leading-6 text-slate-300">
              Please refresh and try again. If the issue persists, check backend availability.
            </p>
            <a
              href="/"
              className="mt-6 inline-flex items-center rounded-full bg-cyan-300 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200"
            >
              Return to home
            </a>
          </section>
        </div>
      );
    }

    return this.props.children;
  }
}

