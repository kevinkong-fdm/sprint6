export type NotificationError = {
  code: string;
  message: string;
};

const fallback: NotificationError = {
  code: "NOTIFY-UNKNOWN",
  message: "Unexpected notification operation error.",
};

const codeToMessage: Record<string, string> = {
  "NOTIFY-001": "Notification preference update is not supported in this version.",
  "NOTIFY-002": "Notification dispatch failed after retry policy.",
  "AUTH-FEAT-001": "Authentication required or session invalid.",
  "AUTH-FEAT-002": "Feature access forbidden for authenticated caller.",
  "AUTH-TOKEN-001": "Session expired. Please sign in again.",
  "AUTH-TOKEN-002": "Session security policy triggered. Please sign in again.",
  "AUTH-SESSION-001": "Session is no longer active. Please sign in again.",
};

export function mapNotificationApiError(error: unknown): NotificationError {
  if (typeof error !== "object" || error === null) {
    return fallback;
  }

  const record = error as Record<string, unknown>;
  const status = typeof record.status === "number" ? record.status : 0;
  const code = typeof record.errorCode === "string" ? record.errorCode : fallback.code;

  if (code === fallback.code) {
    if (status === 401) {
      return {
        code: "AUTH-FEAT-001",
        message: codeToMessage["AUTH-FEAT-001"],
      };
    }

    if (status === 403) {
      return {
        code: "AUTH-FEAT-002",
        message: codeToMessage["AUTH-FEAT-002"],
      };
    }
  }

  return {
    code,
    message: codeToMessage[code] ?? fallback.message,
  };
}

export function isSessionRecoveryRequired(code: string): boolean {
  return (
    code === "AUTH-FEAT-001" ||
    code === "AUTH-FEAT-002" ||
    code === "AUTH-TOKEN-001" ||
    code === "AUTH-TOKEN-002" ||
    code === "AUTH-SESSION-001"
  );
}
