import { FormEvent, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  CustomerResponse,
  CustomerUpdateRequest,
  deleteCustomer,
  getCustomer,
  updateCustomer,
} from "../api/customers";
import { mapCustomerApiError } from "../api/errorMapper";
import { useAuthSession } from "../../auth/session/AuthSessionContext";

export function CustomersPage() {
  const navigate = useNavigate();
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const sessionUserId = session?.userId?.trim() ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [lookupCustomerId, setLookupCustomerId] = useState("");
  const [updateGivenName, setUpdateGivenName] = useState("");
  const [updateFamilyName, setUpdateFamilyName] = useState("");
  const [updatePhoneNumber, setUpdatePhoneNumber] = useState("");
  const [updateLanguage, setUpdateLanguage] = useState("");

  const [deleteCustomerId, setDeleteCustomerId] = useState("");
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerResponse | null>(null);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");
  const [isLoadingCustomer, setIsLoadingCustomer] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const trimmedLookupCustomerId = lookupCustomerId.trim();
  const trimmedDeleteCustomerId = deleteCustomerId.trim();
  const hasLoadedCustomer = selectedCustomer !== null;
  const loadedCustomerId = selectedCustomer?.customerId?.trim() ?? "";

  const prettyCustomer = useMemo(() => {
    if (!selectedCustomer) {
      return "";
    }
    return JSON.stringify(selectedCustomer, null, 2);
  }, [selectedCustomer]);

  const updatePayload = useMemo(() => buildUpdatePayload(), [
    updateGivenName,
    updateFamilyName,
    updatePhoneNumber,
    updateLanguage,
  ]);

  const hasUpdateChanges = useMemo(() => {
    if (!selectedCustomer) {
      return false;
    }

    return (
      normalizeValue(updateGivenName) !== normalizeValue(selectedCustomer.givenName) ||
      normalizeValue(updateFamilyName) !== normalizeValue(selectedCustomer.familyName) ||
      normalizeValue(updatePhoneNumber) !== normalizeValue(selectedCustomer.phoneNumber) ||
      normalizeValue(updateLanguage) !== normalizeValue(selectedCustomer.preferredLanguage)
    );
  }, [
    selectedCustomer,
    updateGivenName,
    updateFamilyName,
    updatePhoneNumber,
    updateLanguage,
  ]);

  const isBusy = isLoadingCustomer || isUpdating || isDeleting;

  const canLookup = hasAccessToken && trimmedLookupCustomerId.length > 0 && !isBusy;
  const canUpdate = hasAccessToken && hasLoadedCustomer && loadedCustomerId.length > 0 && !isBusy && hasUpdateChanges;
  const canDelete = hasAccessToken && trimmedDeleteCustomerId.length > 0 && !isBusy;

  async function handleLookup(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!trimmedLookupCustomerId) {
      setError("Enter a customer ID to load.");
      return;
    }

    setIsLoadingCustomer(true);

    try {
      const customer = await getCustomer(accessToken, trimmedLookupCustomerId);
      setSelectedCustomer(customer);
      setDeleteCustomerId(customer.customerId);
      hydrateUpdateForm(customer);
      setStatus(`Loaded customer ${customer.customerId}.`);
    } catch (err) {
      handleCustomerError(err, "retrieve", trimmedLookupCustomerId);
    } finally {
      setIsLoadingCustomer(false);
    }
  }

  async function handleUpdate(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!selectedCustomer || !loadedCustomerId) {
      setError("Load a customer before applying updates.");
      return;
    }

    const payload = updatePayload;
    if (!payload) {
      setError("Provide at least one mutable field to update.");
      return;
    }

    if (!hasUpdateChanges) {
      setError("No changes detected in mutable fields.");
      return;
    }

    setIsUpdating(true);
    try {
      const updated = await updateCustomer(accessToken, loadedCustomerId, payload);
      setSelectedCustomer(updated);
      hydrateUpdateForm(updated);
      setStatus(`Updated customer ${updated.customerId}.`);
    } catch (err) {
      handleCustomerError(err, "update", loadedCustomerId);
    } finally {
      setIsUpdating(false);
    }
  }

  async function handleDelete(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!trimmedDeleteCustomerId) {
      setError("Enter a customer ID to delete.");
      return;
    }

    setIsDeleting(true);

    try {
      await deleteCustomer(accessToken, trimmedDeleteCustomerId);

      const deletedOwnAccount = sessionUserId.length > 0 && sessionUserId === trimmedDeleteCustomerId;
      if (deletedOwnAccount) {
        signOut();
        navigate("/login", { replace: true });
        return;
      }

      if (selectedCustomer?.customerId === trimmedDeleteCustomerId) {
        setSelectedCustomer(null);
        clearUpdateForm();
      }
      if (trimmedLookupCustomerId === trimmedDeleteCustomerId) {
        setLookupCustomerId("");
      }
      setDeleteCustomerId("");
      setStatus(`Deleted customer ${trimmedDeleteCustomerId}.`);
    } catch (err) {
      handleCustomerError(err, "delete", trimmedDeleteCustomerId);
    } finally {
      setIsDeleting(false);
    }
  }

  function handleCustomerError(
    err: unknown,
    operation?: "retrieve" | "update" | "delete",
    customerId?: string,
  ) {
    const mapped = mapCustomerApiError(err);
    const scopedMessage = buildScopedCustomerErrorMessage(mapped.code, mapped.message, operation, customerId);
    setError(scopedMessage);

    if (isSessionRecoveryRequired(mapped.code)) {
      signOut();
    }
  }

  function buildScopedCustomerErrorMessage(
    code: string,
    message: string,
    operation?: "retrieve" | "update" | "delete",
    customerId?: string,
  ): string {
    const id = customerId?.trim();
    const withId = id ? ` for customer ID ${id}` : "";

    if (operation === "retrieve") {
      if (code === "CUST-GET-001") {
        return `Unable to retrieve profile${withId}. Confirm the customer ID exists and belongs to your accessible scope.`;
      }
      if (code === "CUST-AUTH-001") {
        return `Unable to retrieve profile${withId}. Your current session is not authorized for this customer.`;
      }
      return `Unable to retrieve profile${withId}. ${message}`;
    }

    if (operation === "update") {
      if (code === "CUST-GET-001") {
        return `Unable to update profile${withId} because the customer was not found.`;
      }
      if (code === "CUST-UPD-001") {
        return `Unable to update profile${withId}. Verify the update fields are valid and try again.`;
      }
      if (code === "CUST-UPD-003") {
        return `Unable to update profile${withId}. One or more requested fields are immutable.`;
      }
      if (code === "CUST-AUTH-001") {
        return `Unable to update profile${withId}. Your current session is not authorized for this customer.`;
      }
      return `Unable to update profile${withId}. ${message}`;
    }

    return message;
  }

  function handleUseLoadedCustomer() {
    if (!selectedCustomer) {
      return;
    }
    setLookupCustomerId(selectedCustomer.customerId);
    setDeleteCustomerId(selectedCustomer.customerId);
    hydrateUpdateForm(selectedCustomer);
    setStatus(`Synced forms from customer ${selectedCustomer.customerId}.`);
    setError("");
  }

  function handleClearUpdate() {
    clearUpdateForm();
    setStatus("Cleared update inputs.");
    setError("");
  }

  function handleCancelUpdate() {
    setSelectedCustomer(null);
    clearUpdateForm();
    setStatus("Canceled update. Load another customer to continue.");
    setError("");
  }

  function buildUpdatePayload(): CustomerUpdateRequest | null {
    const payload: CustomerUpdateRequest = {};

    if (updateGivenName.trim()) {
      payload.givenName = updateGivenName.trim();
    }
    if (updateFamilyName.trim()) {
      payload.familyName = updateFamilyName.trim();
    }
    if (updatePhoneNumber.trim()) {
      payload.phoneNumber = updatePhoneNumber.trim();
    }
    if (updateLanguage.trim()) {
      payload.preferredLanguage = updateLanguage.trim();
    }

    return Object.keys(payload).length > 0 ? payload : null;
  }

  function hydrateUpdateForm(customer: CustomerResponse) {
    setUpdateGivenName(customer.givenName ?? "");
    setUpdateFamilyName(customer.familyName ?? "");
    setUpdatePhoneNumber(customer.phoneNumber ?? "");
    setUpdateLanguage(customer.preferredLanguage ?? "");
  }

  function clearUpdateForm() {
    setUpdateGivenName("");
    setUpdateFamilyName("");
    setUpdatePhoneNumber("");
    setUpdateLanguage("");
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Customers</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Customer operations</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Customer profiles are created during account registration. Retrieve, update, and delete profiles.
        </p>
        {!hasAccessToken ? (
          <p className="mt-4 rounded-xl border border-amber-300/30 bg-amber-400/10 px-4 py-3 text-sm text-amber-100">
            Session token is missing. Sign out and sign in again before running customer operations.
          </p>
        ) : null}
      </header>

      <div className="grid gap-6 xl:grid-cols-2">
        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl xl:col-span-1">
          <h3 className="text-xl font-semibold text-white">Retrieve and update</h3>
          <form onSubmit={handleLookup} className="mt-4 space-y-3">
            <input
              aria-label="Lookup customer id"
              type="text"
              required
              value={lookupCustomerId}
              onChange={(event) => setLookupCustomerId(event.target.value)}
              placeholder="Customer ID"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!canLookup}
              className="inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isLoadingCustomer ? "Loading..." : "Load customer"}
            </button>
          </form>

          {hasLoadedCustomer ? (
            <form onSubmit={handleUpdate} className="mt-4 space-y-3 border-t border-white/10 pt-4">
              <p className="text-xs text-slate-400">
                Updating loaded customer <span className="font-medium text-slate-200">{loadedCustomerId}</span>
              </p>
              <input
                aria-label="Update given name"
                type="text"
                value={updateGivenName}
                onChange={(event) => setUpdateGivenName(event.target.value)}
                placeholder="New given name"
                className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
              />
              <input
                aria-label="Update family name"
                type="text"
                value={updateFamilyName}
                onChange={(event) => setUpdateFamilyName(event.target.value)}
                placeholder="New family name"
                className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
              />
              <input
                aria-label="Update phone number"
                type="text"
                value={updatePhoneNumber}
                onChange={(event) => setUpdatePhoneNumber(event.target.value)}
                placeholder="New phone number"
                className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
              />
              <input
                aria-label="Update preferred language"
                type="text"
                value={updateLanguage}
                onChange={(event) => setUpdateLanguage(event.target.value)}
                placeholder="New preferred language"
                className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
              />
              <div className="flex flex-wrap gap-2">
                <button
                  type="submit"
                  disabled={!canUpdate}
                  className="inline-flex flex-1 items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
                >
                  {isUpdating ? "Updating..." : "Apply update"}
                </button>
                <button
                  type="button"
                  onClick={handleCancelUpdate}
                  disabled={isBusy}
                  className="inline-flex flex-1 items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  Cancel update
                </button>
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={handleUseLoadedCustomer}
                  disabled={!selectedCustomer || isBusy}
                  className="inline-flex items-center justify-center rounded-full bg-white/10 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  Sync from loaded profile
                </button>
                <button
                  type="button"
                  onClick={handleClearUpdate}
                  disabled={isBusy}
                  className="inline-flex items-center justify-center rounded-full bg-white/10 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  Clear fields
                </button>
              </div>
              {!hasUpdateChanges ? (
                <p className="text-xs text-slate-400">No pending mutable-field changes detected.</p>
              ) : null}
            </form>
          ) : (
            <div className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-xs text-slate-400">
              Load a customer profile to unlock the update form.
            </div>
          )}
        </article>

        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl xl:col-span-1">
          <h3 className="text-xl font-semibold text-white">Delete customer</h3>
          <form onSubmit={handleDelete} className="mt-4 space-y-3">
            <input
              aria-label="Delete customer id"
              type="text"
              required
              value={deleteCustomerId}
              onChange={(event) => setDeleteCustomerId(event.target.value)}
              placeholder="Customer ID"
              className="w-full rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
            />
            <button
              type="submit"
              disabled={!canDelete}
              className="inline-flex w-full items-center justify-center rounded-xl bg-rose-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-rose-200 disabled:cursor-not-allowed disabled:bg-rose-200/70"
            >
              {isDeleting ? "Deleting..." : "Delete customer"}
            </button>
            <p className="text-xs text-slate-400">Delete performs irreversible hard removal.</p>
          </form>

          <div className="mt-4 rounded-xl border border-white/10 bg-slate-950/50 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.15em] text-slate-400">Session</p>
            <p className="mt-2 break-all text-sm text-slate-200">
              Signed in as {session?.email ?? "operator"}
            </p>
          </div>
        </article>
      </div>

      {status ? (
        <p role="status" className="rounded-xl border border-emerald-300/30 bg-emerald-400/10 px-4 py-3 text-sm text-emerald-100">
          {status}
        </p>
      ) : null}

      {error ? (
        <p role="alert" className="rounded-xl border border-rose-300/30 bg-rose-400/10 px-4 py-3 text-sm text-rose-100">
          {error}
        </p>
      ) : null}

      {prettyCustomer ? (
        <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
          <h3 className="text-xl font-semibold text-white">Selected customer</h3>
          <div className="mt-4 grid gap-3 rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200 sm:grid-cols-2 lg:grid-cols-4">
            <div>
              <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Customer ID</p>
              <p className="mt-1 break-all font-medium text-white">{selectedCustomer?.customerId}</p>
            </div>
            <div>
              <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Email</p>
              <p className="mt-1 font-medium text-white">{selectedCustomer?.email}</p>
            </div>
            <div>
              <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Given name</p>
              <p className="mt-1 font-medium text-white">{selectedCustomer?.givenName || "-"}</p>
            </div>
            <div>
              <p className="text-xs uppercase tracking-[0.14em] text-slate-400">Family name</p>
              <p className="mt-1 font-medium text-white">{selectedCustomer?.familyName || "-"}</p>
            </div>
          </div>

          <details className="mt-4 rounded-xl border border-white/10 bg-slate-950/70 p-4">
            <summary className="cursor-pointer text-sm font-semibold text-cyan-200">View raw payload</summary>
            <pre className="mt-3 overflow-x-auto text-xs leading-6 text-slate-200">{prettyCustomer}</pre>
          </details>
        </article>
      ) : null}
    </section>
  );
}

function normalizeValue(value: string | undefined): string {
  return (value ?? "").trim();
}

function isSessionRecoveryRequired(code: string): boolean {
  return code === "CUST-AUTH-001" || code === "AUTH-TOKEN-001" || code === "AUTH-TOKEN-002" || code === "AUTH-SESSION-001";
}
