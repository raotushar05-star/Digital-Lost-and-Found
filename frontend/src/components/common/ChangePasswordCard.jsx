import React, { useState } from "react";
import ErrorAlert from "./ErrorAlert.jsx";
import PasswordInput from "./PasswordInput.jsx";
import { authService } from "../../services/authService";
import { extractErrorMessage } from "../../services/apiClient";
import SuccessAlert from "./SuccessAlert.jsx";

const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;

export default function ChangePasswordCard() {
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);
  const [saving, setSaving] = useState(false);

  const weak = next.length > 0 && !PASSWORD_RE.test(next);
  const mismatch = confirm.length > 0 && confirm !== next;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    if (!PASSWORD_RE.test(next) || next !== confirm) {
      setError("Please fix the highlighted fields.");
      return;
    }
    setSaving(true);
    try {
      await authService.changePassword(current, next);
      setCurrent("");
      setNext("");
      setConfirm("");
      setSuccess(true);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="card p-4 mt-4" style={{ maxWidth: 560 }} autoComplete="off">
      <h2 style={{ fontSize: "1.05rem" }} className="mb-3">Change password</h2>
      <ErrorAlert message={error} />
      <SuccessAlert message={success ? "Password changed." : null} />
      <div className="mb-3">
        <label className="form-label">Current password</label>
        <PasswordInput autoComplete="current-password" value={current} onChange={(e) => setCurrent(e.target.value)} />
      </div>
      <div className="mb-3">
        <label className="form-label">New password</label>
        <PasswordInput
          autoComplete="new-password"
          value={next}
          onChange={(e) => setNext(e.target.value)}
          error={weak ? "8 to 64 characters with an uppercase letter, a lowercase letter, a number and a symbol." : null}
        />
      </div>
      <div className="mb-3">
        <label className="form-label">Confirm new password</label>
        <PasswordInput
          autoComplete="new-password"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          error={mismatch ? "Passwords do not match." : null}
        />
      </div>
      <button className="btn btn-primary" disabled={saving || !current}>
        {saving ? "Saving…" : "Change password"}
      </button>
    </form>
  );
}