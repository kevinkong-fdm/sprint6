export type StatementError = {
  code: string;
  message: string;
};

const fallback: StatementError = {
  code: "STMT-UNKNOWN",
  message: "Unable to complete monthly statement request.",
};

const codeToMessage: Record<string, string> = {
  "STMT-001": "Invalid statement month. Use YYYY-MM and avoid future months.",
  "STMT-002": "No statement data found for the requested account and month.",
  "AUTH-FEAT-001": "Authentication required or session invalid.",
  "AUTH-FEAT-002": "Feature access forbidden for authenticated caller.",
  "AUTH-TOKEN-001": "Session expired. Please sign in again.",
  "AUTH-TOKEN-002": "Session security policy triggered. Please sign in again.",
  "AUTH-SESSION-001": "Session is no longer active. Please sign in again.",
};

export function mapStatementApiError(error: unknown): StatementError {
  if (typeof error !== "object" || error === null) {
    return fallback;
  }

  const record = error as Record<string, unknown>;
  const status = typeof record.status === "number" ? record.status : 0;
  const code = typeof record.errorCode === "string" ? record.errorCode : fallback.code;
  const responseMessage = typeof record.message === "string" ? record.message.trim() : "";

  if (code === fallback.code) {
    if (status === 401) {
      return {
        code: "AUTH-FEAT-001",
        message: responseMessage || codeToMessage["AUTH-FEAT-001"],
      };
    }

    if (status === 403) {
      return {
        code: "AUTH-FEAT-002",
        message: responseMessage || codeToMessage["AUTH-FEAT-002"],
      };
    }

    if (status === 400) {
      return {
        code: "STMT-001",
        message: responseMessage || codeToMessage["STMT-001"],
      };
    }

    if (status === 404) {
      return {
        code: "STMT-002",
        message: responseMessage || codeToMessage["STMT-002"],
      };
    }
  }

  return {
    code,
    message: responseMessage || codeToMessage[code] || fallback.message,
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
