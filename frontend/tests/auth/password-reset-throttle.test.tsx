import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { PasswordResetRequestForm } from "../../src/features/auth/components/PasswordResetRequestForm";
import { vi, afterEach, describe, it, expect } from "vitest";

describe("password reset throttle", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows throttled message", async () => {
    vi.spyOn(globalThis, "fetch" as never).mockResolvedValue({
      ok: false,
      status: 429,
      json: async () => ({ errorCode: "AUTH-RESET-002" }),
    } as Response);

    render(<PasswordResetRequestForm />);

    await userEvent.type(screen.getByLabelText("Reset email"), "alice@example.com");
    await userEvent.click(screen.getByRole("button", { name: "Request reset" }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Password reset requests are temporarily limited.",
      );
    });
  });
});
