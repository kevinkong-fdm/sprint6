import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { RegistrationForm } from "../../src/features/auth/components/RegistrationForm";
import { vi, afterEach, describe, it, expect } from "vitest";

describe("registration flow", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows success and duplicate error", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch" as never);
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          userId: "u1",
          email: "alice@example.com",
          createdAt: new Date().toISOString(),
        }),
      } as Response)
      .mockResolvedValueOnce({
        ok: false,
        status: 409,
        json: async () => ({ errorCode: "AUTH-REG-002" }),
      } as Response);

    render(<RegistrationForm />);

    await userEvent.type(screen.getByLabelText("Email"), "alice@example.com");
    await userEvent.type(screen.getByLabelText("Password"), "StrongPass!234");
    await userEvent.click(screen.getByRole("button", { name: "Register" }));

    await waitFor(() => {
      expect(screen.getByRole("status")).toHaveTextContent("Registered alice@example.com");
    });

    await userEvent.click(screen.getByRole("button", { name: "Register" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("An account already exists with this email.");
    });
  });
});
