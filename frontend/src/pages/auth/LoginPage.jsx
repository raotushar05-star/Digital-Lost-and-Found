import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { extractErrorMessage } from "../../services/apiClient";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const isEmail = identifier.includes("@");
      const user = await login(isEmail ? { email: identifier, password } : { phone: identifier, password });
      const dest = location.state?.from?.pathname;
      if (dest) {
        navigate(dest);
      } else if (user.role === "POLICE_OFFICER" || user.role === "POLICE_ADMIN") {
        navigate("/police/dashboard");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ minHeight: "100vh" }}>
      <div className="card p-4" style={{ width: 400 }}>
        <div className="page-eyebrow mb-1">Sign in</div>
        <h1 style={{ fontSize: "1.5rem" }} className="mb-3">
          Welcome back
        </h1>
        <form onSubmit={handleSubmit}>
          <ErrorAlert message={error} />
          <div className="mb-3">
            <label className="form-label">Email or phone</label>
            <input
              className="form-control"
              required
              autoFocus
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Password</label>
            <input
              type="password"
              className="form-control"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <button className="btn btn-primary w-100" disabled={submitting}>
            {submitting ? "Signing in…" : "Sign in"}
          </button>
          <p className="text-center text-muted-soft mt-3 mb-0" style={{ fontSize: "0.85rem" }}>
            New here? <Link to="/register">Create an account</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
