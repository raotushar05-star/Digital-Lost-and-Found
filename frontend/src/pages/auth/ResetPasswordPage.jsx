import React, { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import PasswordInput from "../../components/common/PasswordInput.jsx";
import { authService } from "../../services/authService";
import { extractErrorMessage } from "../../services/apiClient";

const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get("token") || "";
  const navigate = useNavigate();
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const weak = password.length > 0 && !PASSWORD_RE.test(password);
  const mismatch = confirm.length > 0 && confirm !== password;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!PASSWORD_RE.test(password) || password !== confirm) {
      setError("Please fix the highlighted fields.");
      return;
    }
    setSubmitting(true);
    try {
      await authService.resetPassword(token, password);
      setDone(true);
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ minHeight: "100vh" }}>
      <div className="card p-4" style={{ width: 400 }}>
        <div className="page-eyebrow mb-1">Account</div>
        <h1 style={{ fontSize: "1.5rem" }} className="mb-3">Choose a new password</h1>
        {!token ? (
          <div className="alert alert-warning">
            This link is incomplete. <Link to="/forgot-password">Request a new one</Link>.
          </div>
        ) : done ? (
          <div className="alert alert-success">Password updated. Redirecting to sign in…</div>
        ) : (
          <form onSubmit={handleSubmit} autoComplete="off">
            <ErrorAlert message={error} />
            <div className="mb-3">
              <label className="form-label">New password</label>
              <PasswordInput
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
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
            <button className="btn btn-primary w-100" disabled={submitting}>
              {submitting ? "Saving…" : "Update password"}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}