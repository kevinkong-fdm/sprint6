import { FormEvent, useState } from "react";
import { registerAccount } from "../api/register";
import { mapApiError } from "../api/errorMapper";

export function RegistrationForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const response = await registerAccount({ email, password });
      setMessage(`Registered ${response.email}`);
    } catch (err) {
      const mapped = mapApiError(err);
      setError(mapped.message);
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <h1>Create account</h1>
      <label>
        Email
        <input
          aria-label="Email"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />
      </label>
      <label>
        Password
        <input
          aria-label="Password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />
      </label>
      <button type="submit">Register</button>
      {message ? <p role="status">{message}</p> : null}
      {error ? <p role="alert">{error}</p> : null}
    </form>
  );
}
