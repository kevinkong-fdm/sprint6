export type CustomerError = {
  code: string;
  message: string;
};

const fallback: CustomerError = {
  code: "CUST-UNKNOWN",
  message: "Unexpected customer operation error.",
};

const codeToMessage: Record<string, string> = {
  "CUST-CRT-001": "Please provide valid customer details before creating the record.",
  "CUST-CRT-002": "A customer already exists with that identity.",
  "CUST-GET-001": "Customer not found.",
  "CUST-UPD-001": "Please provide valid customer updates.",
  "CUST-UPD-003": "You attempted to update an immutable customer field.",
  "CUST-DEL-001": "Customer not found for delete operation.",
  "CUST-DEL-002": "Delete could not complete because dependent records failed to cascade.",
  "CUST-AUTH-001": "Your session is not authorized for this customer action.",
  "AUTH-TOKEN-001": "Session expired. Please sign in again.",
  "AUTH-TOKEN-002": "Session security policy triggered. Please sign in again.",
  "AUTH-SESSION-001": "Session is no longer active. Please sign in again.",
};

export function mapCustomerApiError(error: unknown): CustomerError {
  if (typeof error !== "object" || error === null) {
    return fallback;
  }

  const record = error as Record<string, unknown>;
  const status = typeof record.status === "number" ? record.status : 0;
  const code = typeof record.errorCode === "string" ? record.errorCode : fallback.code;

  if (code === fallback.code && (status === 401 || status === 403)) {
    return {
      code: "CUST-AUTH-001",
      message: codeToMessage["CUST-AUTH-001"],
    };
  }

  return {
    code,
    message: codeToMessage[code] ?? fallback.message,
  };
}
