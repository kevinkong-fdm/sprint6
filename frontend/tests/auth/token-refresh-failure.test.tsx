import { describe, expect, it, vi, afterEach } from "vitest";
import { refreshToken } from "../../src/features/auth/api/refreshToken";

describe("token refresh failure", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("throws when token is invalid", async () => {
    vi.spyOn(globalThis, "fetch" as never).mockResolvedValue({
      ok: false,
      status: 401,
      json: async () => ({ errorCode: "AUTH-TOKEN-001", message: "Invalid or expired refresh token." }),
    } as Response);

    await expect(refreshToken("invalid-refresh-token-value-1234567890")).rejects.toMatchObject({
      errorCode: "AUTH-TOKEN-001",
    });
  });
});
