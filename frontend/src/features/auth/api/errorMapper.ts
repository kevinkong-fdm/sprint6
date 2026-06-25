export type AuthError = {
  code: string;
  message: string;
};

const fallback: AuthError = {
  code: "AUTH-UNKNOWN",
  message: "Unexpected authentication error",
};

const codeToMessage: Record<string, string> = {
  "AUTH-REG-001": "Please provide valid registration details.",
  "AUTH-REG-002": "An account already exists with this email.",
  "AUTH-LOGIN-001": "Invalid email or password.",
  "AUTH-LOGIN-002": "Account is temporarily locked.",
  "AUTH-RESET-002": "Password reset requests are temporarily limited.",
  "AUTH-TOKEN-001": "Session expired. Please log in again.",
  "AUTH-TOKEN-002": "Session security policy triggered. Please log in again.",
  "AUTH-SESSION-001": "Session is no longer active.",
};

export function mapApiError(error: unknown): AuthError {
  if (typeof error !== "object" || error === null) {
    return fallback;
  }

  const record = error as Record<string, unknown>;
  const code = typeof record.errorCode === "string" ? record.errorCode : fallback.code;
  return {
    code,
    message: codeToMessage[code] ?? fallback.message,
  };
}
