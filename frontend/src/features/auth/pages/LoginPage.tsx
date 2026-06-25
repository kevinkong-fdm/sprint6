import { FormEvent, useState } from "react";
import { login } from "../api/login";
import { mapApiError } from "../api/errorMapper";

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const response = await login({ email, password });
      setMessage(`Authenticated. Access token expires in ${response.accessTokenExpiresInSeconds} seconds.`);
    } catch (err) {
      const mapped = mapApiError(err);
      setError(mapped.message);
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <h1>Sign in</h1>
      <label>
        Email
        <input
          aria-label="Login email"
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />
      </label>
      <label>
        Password
        <input
          aria-label="Login password"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />
      </label>
      <button type="submit">Login</button>
      {message ? <p role="status">{message}</p> : null}
      {error ? <p role="alert">{error}</p> : null}
    </form>
  );
}
