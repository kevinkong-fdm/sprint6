export type AccountError = {
  code: string;
  message: string;
};

const fallback: AccountError = {
  code: "ACCT-UNKNOWN",
  message: "Unexpected account operation error.",
};

const codeToMessage: Record<string, string> = {
  "AUTH-ACC-001": "Authentication required or session invalid.",
  "AUTH-ACC-002": "Account access forbidden for authenticated caller.",
  "ACCT-CRT-001": "Account creation validation failed.",
  "ACCT-CRT-002": "Unsupported account type.",
  "ACCT-GET-001": "Account not found.",
  "ACCT-UPD-001": "Account update validation failed.",
  "ACCT-UPD-002": "Immutable account field update attempted.",
  "ACCT-DEL-001": "Account not found for delete operation.",
  "ACCT-DEL-002": "Account delete not allowed due to pending movement activity.",
  "TXN-DEP-001": "Deposit amount validation failed.",
  "TXN-WDR-001": "Withdrawal amount validation failed.",
  "TXN-WDR-002": "Insufficient funds for withdrawal.",
  "TXN-TRF-001": "Transfer amount validation failed.",
  "TXN-TRF-002": "Invalid transfer account pairing.",
  "TXN-TRF-003": "Insufficient funds for transfer.",
  "TXN-HIS-001": "Transaction history filter validation failed.",
  "AUTH-TOKEN-001": "Session expired. Please sign in again.",
  "AUTH-TOKEN-002": "Session security policy triggered. Please sign in again.",
  "AUTH-SESSION-001": "Session is no longer active. Please sign in again.",
};

export function mapAccountApiError(error: unknown): AccountError {
  if (typeof error !== "object" || error === null) {
    return fallback;
  }

  const record = error as Record<string, unknown>;
  const status = typeof record.status === "number" ? record.status : 0;
  const code = typeof record.errorCode === "string" ? record.errorCode : fallback.code;

  if (code === fallback.code) {
    if (status === 401) {
      return {
        code: "AUTH-ACC-001",
        message: codeToMessage["AUTH-ACC-001"],
      };
    }

    if (status === 403) {
      return {
        code: "AUTH-ACC-002",
        message: codeToMessage["AUTH-ACC-002"],
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
    code === "AUTH-ACC-001" ||
    code === "AUTH-ACC-002" ||
    code === "AUTH-TOKEN-001" ||
    code === "AUTH-TOKEN-002" ||
    code === "AUTH-SESSION-001"
  );
}
