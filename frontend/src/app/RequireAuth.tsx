import { Navigate, useLocation } from "react-router-dom";
import { useAuthSession } from "../features/auth/session/AuthSessionContext";

export function RequireAuth({ children }: { children: JSX.Element }) {
  const location = useLocation();
  const { isAuthenticated } = useAuthSession();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}
