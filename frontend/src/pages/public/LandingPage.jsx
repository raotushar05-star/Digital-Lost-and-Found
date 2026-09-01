import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";

export default function LandingPage() {
  const { isAuthenticated, isPolice } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="container-fluid d-flex align-items-center justify-content-between py-2 px-3">
          <span className="brand-mark">
            <span className="brand-crest">LF</span>
            <span>Lost &amp; Found Network</span>
          </span>
          <div className="d-flex gap-2">
            {isAuthenticated ? (
              <Link to={isPolice ? "/police/dashboard" : "/dashboard"} className="btn btn-primary btn-sm">
                Go to dashboard
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-outline-primary btn-sm">
                  Sign in
                </Link>
                <Link to="/register" className="btn btn-primary btn-sm">
                  Create account
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="container py-5">
        <div className="row align-items-center g-5">
          <div className="col-lg-7">
            <div className="page-eyebrow mb-2">A civic service, run with police stations</div>
            <h1 style={{ fontSize: "2.6rem", lineHeight: 1.15 }}>
              Report it lost.
              <br />
              Find it verified.
            </h1>
            <p className="text-muted mt-3" style={{ fontSize: "1.05rem", maxWidth: 520 }}>
              A shared record between citizens and police for lost and found property — from the moment
              something goes missing, through police custody and verification, to a documented handover
              back to its rightful owner.
            </p>
            <div className="d-flex gap-2 mt-4">
              <Link to="/register" className="btn btn-primary">
                Report a lost item
              </Link>
              <Link to="/search" className="btn btn-outline-primary">
                Search found items
              </Link>
            </div>
          </div>
          <div className="col-lg-5">
            <div className="card p-4">
              <div className="page-eyebrow mb-2">How a case moves</div>
              <div className="ledger-timeline">
                {[
                  ["Reported", "Citizen or finder submits a report"],
                  ["Verified by police", "An officer confirms custody details"],
                  ["Matched", "The system surfaces likely pairs"],
                  ["Claimed & verified", "Ownership evidence is reviewed"],
                  ["Returned", "A documented handover closes the case"]
                ].map(([label, meta], idx) => (
                  <div className={`ledger-step ${idx < 2 ? "is-done" : ""}`} key={label}>
                    <div className="ledger-dot" />
                    <div>
                      <div className="ledger-label">{label}</div>
                      <div className="ledger-meta">{meta}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
