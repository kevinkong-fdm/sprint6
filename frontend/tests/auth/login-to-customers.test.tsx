import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { LoginPage } from "../../src/features/auth/pages/LoginPage";
import { AuthSessionProvider } from "../../src/features/auth/session/AuthSessionContext";

describe("login to customers route", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    window.localStorage.clear();
  });

  it("stores auth session and redirects to customers", async () => {
    vi.spyOn(globalThis, "fetch" as never).mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        tokenType: "Bearer",
        accessToken: "access-token-1",
        refreshToken: "refresh-token-1",
        accessTokenExpiresInSeconds: 3600,
        refreshTokenExpiresInSeconds: 2592000,
      }),
    } as Response);

    render(
      <AuthSessionProvider>
        <MemoryRouter initialEntries={["/login"]}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/customers" element={<div>Customers destination</div>} />
          </Routes>
        </MemoryRouter>
      </AuthSessionProvider>,
    );

    await userEvent.type(screen.getByLabelText("Login email"), "alice@example.com");
    await userEvent.type(screen.getByLabelText("Login password"), "StrongPass!234");
    await userEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => {
      expect(screen.getByText("Customers destination")).toBeInTheDocument();
    });

    const persistedSession = window.localStorage.getItem("banking.auth.session");
    expect(persistedSession).not.toBeNull();
    expect(persistedSession).toContain("access-token-1");
  });
});
