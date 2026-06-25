import { AppRoutes } from "./routes";
import { ErrorBoundary } from "./ErrorBoundary";

export function App() {
  return (
    <ErrorBoundary>
      <AppRoutes />
    </ErrorBoundary>
  );
}
