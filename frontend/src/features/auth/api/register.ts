import { apiRequest } from "../../../api/client";

export type RegisterRequest = {
  email: string;
  password: string;
};

export type RegisterResponse = {
  userId: string;
  email: string;
  createdAt: string;
};

export async function registerAccount(payload: RegisterRequest) {
  return apiRequest<RegisterResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
