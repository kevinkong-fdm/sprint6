import { apiRequest } from "../../../api/client";
import type { AuthTokenResponse } from "./login";

export async function refreshToken(refreshToken: string) {
  return apiRequest<AuthTokenResponse>("/auth/token/refresh", {
    method: "POST",
    body: JSON.stringify({ refreshToken }),
  });
}
