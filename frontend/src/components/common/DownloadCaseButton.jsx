import React, { useState } from "react";
import { downloadCaseFilePdf } from "../../utils/caseFilePdf";

export default function DownloadCaseButton({ loadFile, label = "Download this case", className = "btn btn-sm btn-outline-primary" }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const handleClick = async (event) => {
    event.preventDefault();
    event.stopPropagation();
    setBusy(true);
    setError(null);
    try {
      const caseFile = await loadFile();
      await downloadCaseFilePdf(caseFile);
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || "Could not download this case.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <span className="d-inline-flex flex-column align-items-end">
      <button type="button" className={className} onClick={handleClick} disabled={busy}>
        {busy ? "Preparing…" : label}
      </button>
      {error && <span className="text-danger" style={{ fontSize: "0.72rem" }}>{error}</span>}
    </span>
  );
}
