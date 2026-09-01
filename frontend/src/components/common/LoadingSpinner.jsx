import React from "react";

export default function LoadingSpinner({ label = "Loading…" }) {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center py-5 text-muted-soft">
      <div className="spinner-border" style={{ color: "var(--brand)" }} role="status" />
      <div className="mt-2 font-mono" style={{ fontSize: "0.8rem" }}>{label}</div>
    </div>
  );
}
