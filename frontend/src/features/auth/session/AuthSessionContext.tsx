import { createContext, ReactNode, useContext, useMemo, useState } from "react";

export type AuthSession = {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresInSeconds: number;
  refreshTokenExpiresInSeconds: number;
  userId?: string;
  email?: string;
};

type AuthSessionContextValue = {
  session: AuthSession | null;
  isAuthenticated: boolean;
  signIn: (nextSession: AuthSession) => void;
  signOut: () => void;
};

const STORAGE_KEY = "banking.auth.session";

const AuthSessionContext = createContext<AuthSessionContextValue | undefined>(undefined);

export function AuthSessionProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => readSessionFromStorage());

  const value = useMemo<AuthSessionContextValue>(() => {
    return {
      session,
      isAuthenticated: session !== null && session.accessToken.trim().length > 0,
      signIn: (nextSession) => {
        setSession(nextSession);
        writeSessionToStorage(nextSession);
      },
      signOut: () => {
        setSession(null);
        clearSessionFromStorage();
      },
    };
  }, [session]);

  return <AuthSessionContext.Provider value={value}>{children}</AuthSessionContext.Provider>;
}

export function useAuthSession() {
  const context = useContext(AuthSessionContext);
  if (!context) {
    throw new Error("useAuthSession must be used within an AuthSessionProvider.");
  }
  return context;
}

function readSessionFromStorage(): AuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<AuthSession>;
    if (!parsed.accessToken || !parsed.refreshToken || !parsed.tokenType) {
      return null;
    }

    return {
      tokenType: parsed.tokenType,
      accessToken: parsed.accessToken,
      refreshToken: parsed.refreshToken,
      accessTokenExpiresInSeconds: Number(parsed.accessTokenExpiresInSeconds ?? 0),
      refreshTokenExpiresInSeconds: Number(parsed.refreshTokenExpiresInSeconds ?? 0),
      userId: parsed.userId,
      email: parsed.email,
    };
  } catch {
    return null;
  }
}

function writeSessionToStorage(session: AuthSession) {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

function clearSessionFromStorage() {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.removeItem(STORAGE_KEY);
}
