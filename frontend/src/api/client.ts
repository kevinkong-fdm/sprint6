const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const AUTH_SESSION_STORAGE_KEY = "banking.auth.session";
const TOKEN_REFRESH_PATH = "/auth/token/refresh";

type StoredAuthSession = {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresInSeconds: number;
  refreshTokenExpiresInSeconds: number;
  email?: string;
};

let refreshInFlight: Promise<string | null> | null = null;

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const hasBody = options.body !== undefined && !(options.body instanceof FormData);
  const headers = buildHeaders(options.headers, hasBody);

  let response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if ((response.status === 401 || response.status === 403) && shouldAttemptTokenRefresh(path, headers)) {
    const refreshedAccessToken = await refreshAccessToken();
    if (refreshedAccessToken) {
      headers.set("Authorization", `Bearer ${refreshedAccessToken}`);
      response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
      });
    }
  }

  if (!response.ok) {
    const payload = await readResponsePayload<Record<string, unknown>>(response);
    throw {
      status: response.status,
      ...(payload ?? {}),
    };
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const payload = await readResponsePayload<T>(response);
  return payload as T;
}

function shouldAttemptTokenRefresh(path: string, headers: Headers): boolean {
  if (path.startsWith("/auth/")) {
    return false;
  }

  const authorization = headers.get("Authorization");
  if (!authorization) {
    return false;
  }

  return authorization.startsWith("Bearer ") && authorization.length > "Bearer ".length;
}

async function refreshAccessToken(): Promise<string | null> {
  if (refreshInFlight) {
    return refreshInFlight;
  }

  refreshInFlight = (async () => {
    const session = readSessionFromStorage();
    if (!session || !session.refreshToken) {
      return null;
    }

    const response = await fetch(`${API_BASE_URL}${TOKEN_REFRESH_PATH}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ refreshToken: session.refreshToken }),
    });

    if (!response.ok) {
      clearSessionFromStorage();
      return null;
    }

    const payload = await readResponsePayload<Partial<StoredAuthSession>>(response);
    if (!payload || typeof payload.accessToken !== "string" || typeof payload.refreshToken !== "string" || typeof payload.tokenType !== "string") {
      clearSessionFromStorage();
      return null;
    }

    const nextSession: StoredAuthSession = {
      ...session,
      tokenType: payload.tokenType,
      accessToken: payload.accessToken,
      refreshToken: payload.refreshToken,
      accessTokenExpiresInSeconds: Number(payload.accessTokenExpiresInSeconds ?? session.accessTokenExpiresInSeconds ?? 0),
      refreshTokenExpiresInSeconds: Number(payload.refreshTokenExpiresInSeconds ?? session.refreshTokenExpiresInSeconds ?? 0),
      email: session.email,
    };

    writeSessionToStorage(nextSession);
    return nextSession.accessToken;
  })().finally(() => {
    refreshInFlight = null;
  });

  return refreshInFlight;
}

function buildHeaders(source: RequestInit["headers"], hasBody: boolean): Headers {
  const headers = new Headers(source);
  if (hasBody && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  return headers;
}

async function readResponsePayload<T>(response: Response): Promise<T | undefined> {
  const textReader = (response as unknown as { text?: () => Promise<string> }).text;
  if (typeof textReader === "function") {
    const text = await textReader.call(response);
    if (!text) {
      return undefined;
    }

    try {
      return JSON.parse(text) as T;
    } catch {
      return undefined;
    }
  }

  const jsonReader = (response as unknown as { json?: () => Promise<unknown> }).json;
  if (typeof jsonReader === "function") {
    return (await jsonReader.call(response)) as T;
  }

  return undefined;
}

function readSessionFromStorage(): StoredAuthSession | null {
  if (typeof window === "undefined") {
    return null;
  }

  const raw = window.localStorage.getItem(AUTH_SESSION_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<StoredAuthSession>;
    if (!parsed.accessToken || !parsed.refreshToken || !parsed.tokenType) {
      return null;
    }

    return {
      tokenType: parsed.tokenType,
      accessToken: parsed.accessToken,
      refreshToken: parsed.refreshToken,
      accessTokenExpiresInSeconds: Number(parsed.accessTokenExpiresInSeconds ?? 0),
      refreshTokenExpiresInSeconds: Number(parsed.refreshTokenExpiresInSeconds ?? 0),
      email: parsed.email,
    };
  } catch {
    return null;
  }
}

function writeSessionToStorage(session: StoredAuthSession) {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
}

function clearSessionFromStorage() {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
}
