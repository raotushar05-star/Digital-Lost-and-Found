import React from "react";
import { Link } from "react-router-dom";
import { formatDate } from "../../utils/format";
import { getStatusMeta } from "../../utils/statusConfig";

export default function DuplicateWarning({ hits, onProceed, onCancel, busy }) {
  return (
    <div className="alert alert-warning">
      <div className="fw-medium mb-1">You may have already reported this</div>
      <p className="mb-2" style={{ fontSize: "0.85rem" }}>
        You have an open report that looks very similar. Filing it again can slow police down and create duplicate cases.
      </p>
      <ul className="mb-3" style={{ fontSize: "0.85rem" }}>
        {hits.map((h) => (
          <li key={h.id}>
            {h.category} · {h.description} · {formatDate(h.date)} · {h.city} · {getStatusMeta(h.status).label}
            {h.kind === "LOST" && <> · <Link to={`/lost-items/${h.id}`}>View</Link></>}
          </li>
        ))}
      </ul>
      <div className="d-flex gap-2">
        <button type="button" className="btn btn-sm btn-outline-secondary" onClick={onCancel}>Go back</button>
        <button type="button" className="btn btn-sm btn-primary" disabled={busy} onClick={onProceed}>
          It is a different item, submit anyway
        </button>
      </div>
    </div>
  );
}