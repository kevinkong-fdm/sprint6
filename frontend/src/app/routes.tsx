import { Navigate, Route, Routes } from "react-router-dom";
import { RegisterPage } from "../features/auth/pages/RegisterPage";
import { LoginPage } from "../features/auth/pages/LoginPage";
import { PasswordResetRequestPage } from "../features/auth/pages/PasswordResetRequestPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/password-reset" element={<PasswordResetRequestPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
