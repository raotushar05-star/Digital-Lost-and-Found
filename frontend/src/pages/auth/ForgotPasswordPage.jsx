import React, { useState } from "react";
import { Link } from "react-router-dom";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { authService } from "../../services/authService";
import { extractErrorMessage } from "../../services/apiClient";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const res = await authService.forgotPassword(email.trim());
      setMessage(res.message);
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
        <h1 style={{ fontSize: "1.5rem" }} className="mb-3">Forgot password</h1>
        {message ? (
          <div className="alert alert-success">{message}</div>
        ) : (
          <form onSubmit={handleSubmit}>
            <ErrorAlert message={error} />
            <p className="text-muted-soft" style={{ fontSize: "0.9rem" }}>
              Enter the email on your account. We will send a link that works for 30 minutes.
            </p>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className="form-control" required autoFocus value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <button className="btn btn-primary w-100" disabled={submitting}>
              {submitting ? "Sending…" : "Send reset link"}
            </button>
          </form>
        )}
        <p className="text-center text-muted-soft mt-3 mb-0" style={{ fontSize: "0.85rem" }}>
          <Link to="/login">Back to sign in</Link>
        </p>
      </div>
    </div>
  );
}