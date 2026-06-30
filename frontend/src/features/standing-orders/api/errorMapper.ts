export type StandingOrderError = {
  code: string;
  message: string;
};

const fallback: StandingOrderError = {
  code: "SO-UNKNOWN",
  message: "Unexpected standing-order operation error.",
};

const codeToMessage: Record<string, string> = {
  "SO-SET-001": "Standing-order setup validation failed.",
  "SO-SET-002": "Standing-order schedule configuration invalid.",
  "SO-SET-003": "Standing-order source account unauthorized.",
  "SO-SET-004": "Standing-order destination must be an internal platform account.",
  "SO-SET-005": "Standing-order destination account is not owned by the authenticated customer.",
  "SO-UPD-001": "Standing-order update validation failed. Check required fields and schedule settings.",
  "SO-UPD-002": "This standing order cannot be updated because it is canceled.",
  "SO-DEL-001": "Standing-order not found.",
  "SO-EXE-001": "Standing-order execution failed due to insufficient funds.",
  "SO-EXE-002": "Execution skipped. Ensure the standing order and source account are active before triggering.",
  "AUTH-FEAT-001": "Authentication required or session invalid.",
  "AUTH-FEAT-002": "Feature access forbidden for authenticated caller.",
  "AUTH-TOKEN-001": "Session expired. Please sign in again.",
  "AUTH-TOKEN-002": "Session security policy triggered. Please sign in again.",
  "AUTH-SESSION-001": "Session is no longer active. Please sign in again.",
};

export function mapStandingOrderApiError(error: unknown): StandingOrderError {
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
