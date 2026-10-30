import React, { useState } from "react";
import ErrorAlert from "../common/ErrorAlert.jsx";
import { policeService } from "../../services/policeService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDateTime } from "../../utils/format";

const METHODS = [
  ["AUCTIONED", "Auctioned"],
  ["DONATED", "Donated"],
  ["DESTROYED", "Destroyed"],
  ["HANDED_TO_AUTHORITY", "Handed to another authority"]
];

export default function DisposalCard({ item, onDone }) {
  const [method, setMethod] = useState("");
  const [notes, setNotes] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  if (item.custodyStatus === "DISPOSED") {
    return (
      <div className="card p-3 mt-3">
        <h2 style={{ fontSize: "1rem" }} className="mb-2">Disposed</h2>
        <div style={{ fontSize: "0.88rem" }}>
          {METHODS.find(([v]) => v === item.disposalMethod)?.[1] || item.disposalMethod} on {formatDateTime(item.disposedAt)}
        </div>
        {item.disposalNotes && <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>{item.disposalNotes}</div>}
      </div>
    );
  }
  if (item.custodyStatus !== "IN_CUSTODY") return null;

  const submit = async (e) => {
    e.preventDefault();
    if (!window.confirm("Mark this item as disposed? This cannot be undone.")) return;
    setError(null);
    setBusy(true);
    try {
      await policeService.disposeItem(item.foundItemId, { method, notes: notes.trim() });
      onDone();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="card p-3 mt-3">
      <h2 style={{ fontSize: "1rem" }} className="mb-1">Retention</h2>
      <div className="text-muted-soft mb-2" style={{ fontSize: "0.82rem" }}>
        In custody for {item.daysInCustody} day(s).{" "}
        {item.overdue ? "The retention period has passed." : "Disposal opens once the retention period passes."}
      </div>
      {item.overdue && (
        <form onSubmit={submit}>
          <ErrorAlert message={error} />
          <div className="mb-2">
            <label className="form-label">Disposal method</label>
            <select className="form-select" required value={method} onChange={(e) => setMethod(e.target.value)}>
              <option value="">Select…</option>
              {METHODS.map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </div>
          <div className="mb-2">
            <label className="form-label">Notes (optional)</label>
            <textarea className="form-control" rows={2} maxLength={1000} value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>
          <button className="btn btn-sm btn-outline-danger" disabled={busy || !method}>
            {busy ? "Saving…" : "Mark as disposed"}
          </button>
        </form>
      )}
    </div>
  );
}