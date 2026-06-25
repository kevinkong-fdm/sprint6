import { FormEvent, useState } from "react";
import { mapApiError } from "../api/errorMapper";
import { requestPasswordReset } from "../api/passwordResetRequest";

export function PasswordResetRequestForm() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const response = await requestPasswordReset({ email });
      setMessage(response.message);
    } catch (err) {
      const mapped = mapApiError(err);
      setError(mapped.message);
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <h1>Password reset</h1>
      <label>
        Email
        <input
          aria-label="Reset email"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />
      </label>
      <button type="submit">Request reset</button>
      {message ? <p role="status">{message}</p> : null}
      {error ? <p role="alert">{error}</p> : null}
    </form>
  );
}
