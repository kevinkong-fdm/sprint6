import { useMemo } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { PaymentActivityPopups } from "../features/account/components/PaymentActivityPopups";
import { useAuthSession } from "../features/auth/session/AuthSessionContext";

export function AppShell() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, session, signOut } = useAuthSession();
  const isAuthenticatedFeatureRoute = [
    "/accounts",
    "/customers",
    "/standing-orders",
    "/statements",
    "/insights",
  ].includes(location.pathname);
  const showNavigation = isAuthenticated && isAuthenticatedFeatureRoute;

  const navigationItems = useMemo(() => {
    const items = [{ to: "/", label: "Overview" }];
    if (isAuthenticated) {
      items.push({ to: "/accounts", label: "Accounts" });
      items.push({ to: "/customers", label: "Customers" });
      items.push({ to: "/standing-orders", label: "Standing orders" });
      items.push({ to: "/statements", label: "Statements" });
      items.push({ to: "/insights", label: "Insights" });
    } else {
      return [];
    }
    return items;
  }, [isAuthenticated]);

  function handleSignOut() {
    signOut();
    navigate("/login");
  }

  return (
    <div className="relative min-h-screen overflow-x-hidden">
      <PaymentActivityPopups />

      <div aria-hidden="true" className="pointer-events-none absolute inset-0">
        <div className="absolute -left-36 top-16 h-72 w-72 rounded-full bg-cyan-300/20 blur-3xl" />
        <div className="absolute right-0 top-0 h-96 w-96 rounded-full bg-emerald-300/10 blur-3xl" />
        <div className="absolute bottom-0 left-1/3 h-80 w-80 rounded-full bg-blue-300/10 blur-3xl" />
      </div>

      <div className="relative mx-auto flex min-h-screen w-full max-w-6xl flex-col px-4 py-6 sm:px-6 lg:px-8">
        <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/55 p-4 shadow-soft backdrop-blur-xl sm:p-5">
          <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">
                  Digital Bank
                </p>
                <h1 className="mt-2 text-2xl font-semibold text-white sm:text-3xl">
                  Console
                </h1>
                <p className="mt-1 max-w-2xl text-sm text-slate-200/80">
                  Manage sign in, account recovery, and authenticated customer operations from one secure workspace.
                </p>
                {isAuthenticated ? (
                  <p className="mt-2 text-xs font-medium text-cyan-200/80">
                    Signed in as {session?.email ?? "operator"}
                  </p>
                ) : null}
              </div>

              {isAuthenticated ? (
                <button
                  type="button"
                  onClick={handleSignOut}
                  className="rounded-full bg-rose-300 px-3 py-2 text-xs font-semibold text-slate-950 transition hover:bg-rose-200 sm:px-4 sm:text-sm"
                >
                  Logout
                </button>
              ) : null}
            </div>

            {showNavigation ? (
              <div className="rounded-2xl border border-white/10 bg-slate-950/45 p-2">
                <nav
                  aria-label="Primary"
                  className="flex min-w-0 flex-nowrap items-center gap-2 overflow-x-auto whitespace-nowrap pr-1 [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden"
                >
                  {navigationItems.map((item) => (
                    <NavLink
                      key={item.to}
                      to={item.to}
                      className={({ isActive }) =>
                        [
                          "shrink-0 rounded-full px-3 py-2 text-xs font-semibold transition duration-200 sm:px-4 sm:text-sm",
                          isActive
                            ? "bg-cyan-300 text-slate-950"
                            : "bg-white/5 text-slate-200 hover:bg-white/15 hover:text-white",
                        ].join(" ")
                      }
                    >
                      {item.label}
                    </NavLink>
                  ))}
                </nav>
              </div>
            ) : null}
          </div>
        </header>

        <main className="relative mt-8 flex-1 pb-10">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
