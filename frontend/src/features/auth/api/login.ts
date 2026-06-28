import { apiRequest } from "../../../api/client";

export type LoginRequest = {
  email: string;
  password: string;
};

export type AuthTokenResponse = {
  tokenType: string;
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresInSeconds: number;
  refreshTokenExpiresInSeconds: number;
  userId: string;
};

export async function login(payload: LoginRequest) {
  return apiRequest<AuthTokenResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
