import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import GoogleVerifyButton, { googleEnabled, decodeGoogleCredential } from "../../components/auth/GoogleVerifyButton.jsx";
import { extractErrorMessage } from "../../services/apiClient";
import { NAME_RE, EMAIL_RE, PHONE_RE, PASSWORD_RE } from "../../utils/validators";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", phone: "", password: "", confirmPassword: "", address: "" });
  const [credential, setCredential] = useState(null);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);
  const [hints, setHints] = useState({ name: null, phone: null });

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });
  // Clean the value, and explain if something was removed
  const cleanField = (field, cleaner, message) => (e) => {
    const raw = e.target.value;
    const cleaned = cleaner(raw);
    setForm((f) => ({ ...f, [field]: cleaned }));
    setHints((h) => ({ ...h, [field]: cleaned !== raw ? message : null }));
  };

  const handleCredential = (token) => {
    try {
      const info = decodeGoogleCredential(token);
      setForm((f) => ({ ...f, email: info.email || "", name: f.name || info.name || "" }));
      setCredential(token);
      setError(null);
    } catch {
      setError("Could not read the Google sign-in. Please try again.");
    }
  };

  const resetGoogle = () => {
    setCredential(null);
    setForm((f) => ({ ...f, email: "" }));
  };

  const mismatch = form.confirmPassword.length > 0 && form.password !== form.confirmPassword;
  const nameMsg = form.name && !NAME_RE.test(form.name.trim()) ? "Name can contain letters and spaces only." : null;
  const phoneMsg = form.phone && !PHONE_RE.test(form.phone) ? "Phone must be exactly 10 digits, numbers only." : null;
  const phoneShort = form.phone.length > 0 && form.phone.length < 10 ? "Phone must be exactly 10 digits." : null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!NAME_RE.test(form.name.trim())) return setError("Name: letters and spaces only.");
    if (form.email && !EMAIL_RE.test(form.email.trim())) return setError("Enter a valid email like name@example.com.");
    if (!PHONE_RE.test(form.phone)) return setError("Phone must be exactly 10 digits.");
    if (!PASSWORD_RE.test(form.password)) {
      return setError("Password needs 8 to 64 characters with an uppercase letter, a lowercase letter, a number and a symbol.");
    }
    if (form.password !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setSubmitting(true);
    try {
      const { confirmPassword, ...payload } = form;
      await register(credential ? { ...payload, googleCredential: credential } : payload);
      setDone(true);
      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      const message = extractErrorMessage(err);
      setError(message);
      // an expired or rejected Google token has to be redone
      if (credential && /google/i.test(message)) setCredential(null);
    } finally {
      setSubmitting(false);
    }
  };

  const needsGoogle = googleEnabled && !credential;

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ minHeight: "100vh" }}>
      <div className="card p-4" style={{ width: 420 }}>
        <div className="page-eyebrow mb-1">Create account</div>
        <h1 style={{ fontSize: "1.5rem" }} className="mb-3">
          Join FindLine
        </h1>
        {done ? (
          <div className="alert alert-success">Account created. Redirecting to sign in…</div>
        ) : needsGoogle ? (
          <div>
            <ErrorAlert message={error} />
            <p className="text-muted-soft" style={{ fontSize: "0.9rem" }}>
              First, confirm your email with Google. You will then add your phone number and a password.
            </p>
            <GoogleVerifyButton onCredential={handleCredential} />
            <p className="text-center text-muted-soft mt-3 mb-0" style={{ fontSize: "0.85rem" }}>
              Already have an account? <Link to="/login">Sign in</Link>
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} autoComplete="off">
            <ErrorAlert message={error} />
            <div className="mb-3">
              <label className="form-label">Full name</label>
              <input
                className={`form-control ${hints.name ? "is-invalid" : ""}`}
                required
                autoComplete="off"
                value={form.name}
                onChange={cleanField(
                  "name",
                  (v) => v.replace(/[^\p{L} .'-]/gu, ""),
                  "Names can contain letters and spaces only. Numbers and symbols are not allowed."
                )}
              />
              {hints.name && <div className="invalid-feedback d-block">{hints.name}</div>}
              {nameMsg && <div className="invalid-feedback d-block">{nameMsg}</div>}
            </div>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-control"
                autoComplete="off"
                readOnly={Boolean(credential)}
                value={form.email}
                onChange={update("email")}
              />
              {credential && (
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>
                  Verified with Google.{" "}
                  <button type="button" className="btn btn-link btn-sm p-0 align-baseline" onClick={resetGoogle}>
                    Use a different account
                  </button>
                </div>
              )}
            </div>
            <div className="mb-3">
              <label className="form-label">Phone number</label>
              <input
                className={`form-control ${(hints.phone || phoneShort) ? "is-invalid" : ""}`}
                required
                autoComplete="off"
                inputMode="numeric"
                value={form.phone}
                onChange={cleanField(
                  "phone",
                  (v) => v.replace(/\D/g, "").slice(0, 10),
                  "Phone must be digits only, up to 10 digits."
                )}
              />
              {(hints.phone || phoneShort) && <div className="invalid-feedback d-block">{(hints.phone || phoneShort)}</div>}
              {phoneMsg && <div className="invalid-feedback d-block">{phoneMsg}</div>}
            </div>
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-control"
                required
                minLength={8}
                autoComplete="new-password"
                value={form.password}
                onChange={update("password")}
              />
              <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>
                At least 8 characters.
              </div>
            </div>
            <div className="mb-3">
              <label className="form-label">Confirm password</label>
              <input
                type="password"
                className={`form-control ${mismatch ? "is-invalid" : ""}`}
                required
                autoComplete="new-password"
                value={form.confirmPassword}
                onChange={update("confirmPassword")}
              />
              {mismatch && (
                <div className="invalid-feedback" style={{ display: "block" }}>
                  Passwords do not match.
                </div>
              )}
            </div>
            <div className="mb-3">
              <label className="form-label">Address (optional)</label>
              <textarea className="form-control" rows={2} value={form.address} onChange={update("address")} />
            </div>
            <button className="btn btn-primary w-100" disabled={submitting || mismatch || !!nameMsg || !!phoneMsg}>
              {submitting ? "Creating account…" : "Create account"}
            </button>
            <p className="text-center text-muted-soft mt-3 mb-0" style={{ fontSize: "0.85rem" }}>
              Already have an account? <Link to="/login">Sign in</Link>
            </p>
          </form>
        )}
      </div>
    </div>
  );
}