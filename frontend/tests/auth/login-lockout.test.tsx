import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LoginPage } from "../../src/features/auth/pages/LoginPage";
import { vi, afterEach, describe, it, expect } from "vitest";
import { AuthSessionProvider } from "../../src/features/auth/session/AuthSessionContext";
import { MemoryRouter } from "react-router-dom";

describe("login lockout", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders lockout message", async () => {
    vi.spyOn(globalThis, "fetch" as never).mockResolvedValue({
      ok: false,
      status: 423,
      json: async () => ({ errorCode: "AUTH-LOGIN-002" }),
    } as Response);

    render(
      <AuthSessionProvider>
        <MemoryRouter>
          <LoginPage />
        </MemoryRouter>
      </AuthSessionProvider>,
    );

    await userEvent.type(screen.getByLabelText("Login email"), "alice@example.com");
    await userEvent.type(screen.getByLabelText("Login password"), "badpass");
    await userEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("Account is temporarily locked.");
    });
  });
});
