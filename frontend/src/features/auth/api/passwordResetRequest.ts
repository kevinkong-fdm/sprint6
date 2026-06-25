import { apiRequest } from "../../../api/client";

export type PasswordResetRequest = {
  email: string;
};

export type AcceptedResponse = {
  message: string;
  correlationId: string;
};

export async function requestPasswordReset(payload: PasswordResetRequest) {
  return apiRequest<AcceptedResponse>("/auth/password-reset/request", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
