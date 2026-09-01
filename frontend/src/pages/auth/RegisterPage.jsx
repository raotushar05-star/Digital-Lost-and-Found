import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { extractErrorMessage } from "../../services/apiClient";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", phone: "", password: "", address: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(false);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register(form);
      setDone(true);
      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ minHeight: "100vh" }}>
      <div className="card p-4" style={{ width: 420 }}>
        <div className="page-eyebrow mb-1">Create account</div>
        <h1 style={{ fontSize: "1.5rem" }} className="mb-3">
          Join the Lost &amp; Found Network
        </h1>
        {done ? (
          <div className="alert alert-success">Account created. Redirecting to sign in…</div>
        ) : (
          <form onSubmit={handleSubmit}>
            <ErrorAlert message={error} />
            <div className="mb-3">
              <label className="form-label">Full name</label>
              <input className="form-control" required value={form.name} onChange={update("name")} />
            </div>
            <div className="mb-3">
              <label className="form-label">Email</label>
              <input type="email" className="form-control" value={form.email} onChange={update("email")} />
            </div>
            <div className="mb-3">
              <label className="form-label">Phone number</label>
              <input className="form-control" required value={form.phone} onChange={update("phone")} />
            </div>
            <div className="mb-3">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-control"
                required
                minLength={8}
                value={form.password}
                onChange={update("password")}
              />
              <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>
                At least 8 characters.
              </div>
            </div>
            <div className="mb-3">
              <label className="form-label">Address (optional)</label>
              <textarea className="form-control" rows={2} value={form.address} onChange={update("address")} />
            </div>
            <button className="btn btn-primary w-100" disabled={submitting}>
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
