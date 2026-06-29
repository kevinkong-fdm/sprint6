import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./AppShell";
import { RegisterPage } from "../features/auth/pages/RegisterPage";
import { LoginPage } from "../features/auth/pages/LoginPage";
import { HomePage } from "../features/auth/pages/HomePage";
import { PasswordResetRequestPage } from "../features/auth/pages/PasswordResetRequestPage";
import { CustomersPage } from "../features/customer/pages/CustomersPage";
import { AccountsPage } from "../features/account/pages/AccountsPage";
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
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
