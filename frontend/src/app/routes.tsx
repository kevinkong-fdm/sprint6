import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./AppShell";
import { RegisterPage } from "../features/auth/pages/RegisterPage";
import { LoginPage } from "../features/auth/pages/LoginPage";
import { HomePage } from "../features/auth/pages/HomePage";
import { PasswordResetRequestPage } from "../features/auth/pages/PasswordResetRequestPage";
import { CustomersPage } from "../features/customer/pages/CustomersPage";
import { AccountsPage } from "../features/account/pages/AccountsPage";
import { StandingOrdersPage } from "../features/standing-orders/pages/StandingOrdersPage";
import { NotificationsPage } from "../features/notifications/pages/NotificationsPage";
import { StatementsPage } from "../features/statements/pages/StatementsPage";
import { InsightsPage } from "../features/insights/pages/InsightsPage";
import { RequireAuth } from "./RequireAuth";

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<HomePage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="password-reset" element={<PasswordResetRequestPage />} />
        <Route path="customers" element={<RequireAuth><CustomersPage /></RequireAuth>} />
        <Route path="accounts" element={<RequireAuth><AccountsPage /></RequireAuth>} />
        <Route path="standing-orders" element={<RequireAuth><StandingOrdersPage /></RequireAuth>} />
        <Route path="notifications" element={<RequireAuth><NotificationsPage /></RequireAuth>} />
        <Route path="statements" element={<RequireAuth><StatementsPage /></RequireAuth>} />
        <Route path="insights" element={<RequireAuth><InsightsPage /></RequireAuth>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
