import React, { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { extractErrorMessage } from "../../services/apiClient";
import PasswordInput from "../../components/common/PasswordInput.jsx";

const SIGNIN_OPTIONS = {
  citizen: {
    label: "Citizen",
    heading: "Citizen sign in",
    roles: ["USER"],
    home: "/dashboard"
  },
  police: {
    label: "Police Officer",
    heading: "Police officer sign in",
    roles: ["POLICE_OFFICER"],
    home: "/police/dashboard"
  },
  admin: {
    label: "Admin",
    heading: "Admin sign in",
    roles: ["POLICE_ADMIN", "SYSTEM_ADMIN"],
    home: null // decided by role below
  }
};

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const initial = SIGNIN_OPTIONS[searchParams.get("role")] ? searchParams.get("role") : "citizen";
  const [mode, setMode] = useState(initial);
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [notice] = useState(() => sessionStorage.getItem("lf_notice"));
  useEffect(() => { sessionStorage.removeItem("lf_notice"); }, []);

  const option = SIGNIN_OPTIONS[mode];

  const switchMode = (key) => {
    setMode(key);
    setError(null);
    setSearchParams({ role: key }, { replace: true });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const isEmail = identifier.includes("@");
      const user = await login(
        isEmail ? { email: identifier, password } : { phone: identifier, password },
        option.roles
      );
      const dest = location.state?.from?.pathname;
      if (dest) {
        navigate(dest);
      } else if (user.role === "SYSTEM_ADMIN") {
        navigate("/admin/stations");
      } else if (user.role === "POLICE_ADMIN" || user.role === "POLICE_OFFICER") {
        navigate("/police/dashboard");
      } else {
        navigate("/dashboard");
      }
    } catch (err) {
      setError(
        err.roleMismatch
          ? `This account is not a ${option.label} account. Please choose the correct sign-in option above.`
          : extractErrorMessage(err)
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ minHeight: "100vh" }}>
      <div className="card p-4" style={{ width: 440 }}>
        <div className="page-eyebrow mb-1">Sign in</div>
        <h1 style={{ fontSize: "1.5rem" }} className="mb-3">{option.heading}</h1>

        <ul className="nav nav-pills nav-fill mb-3" role="tablist">
          {Object.entries(SIGNIN_OPTIONS).map(([key, opt]) => (
            <li className="nav-item" key={key}>
              <button
                type="button"
                role="tab"
                aria-selected={mode === key}
                className={`nav-link ${mode === key ? "active" : ""}`}
                style={{ fontSize: "0.85rem" }}
                onClick={() => switchMode(key)}
              >
                {opt.label}
              </button>
            </li>
          ))}
        </ul>

        <form onSubmit={handleSubmit}>
          {notice && <div className="alert alert-info">{notice}</div>}
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
            <PasswordInput
              required
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <div className="text-end mb-3" style={{ fontSize: "0.85rem" }}>
            <Link to="/forgot-password">Forgot password?</Link>
          </div>
          <button className="btn btn-primary w-100" disabled={submitting}>
            {submitting ? "Signing in…" : `Sign in as ${option.label}`}
          </button>
        </form>

        <div className="hairline-top mt-3 pt-3 text-center text-muted-soft" style={{ fontSize: "0.85rem" }}>
          {mode === "citizen" ? (
            <>
              New here?{" "}
              <Link to="/register" className="btn btn-sm btn-outline-primary ms-1">
                Create account
              </Link>
            </>
          ) : (
            <>Police and admin accounts are issued by the System Administrator and can't be self-registered.</>
          )}
        </div>
      </div>
    </div>
  );
}