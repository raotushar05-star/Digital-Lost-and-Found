import React, { useState } from "react";
import { saveBlobResponse, blobErrorMessage } from "../../utils/downloadBlob";

export default function DownloadServerPdfButton({
  load,
  fallbackName = "findline-report.pdf",
  label = "Download PDF",
  className = "btn btn-sm btn-outline-primary"
}) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const handleClick = async (event) => {
    event.preventDefault();
    event.stopPropagation();
    setBusy(true);
    setError(null);
    try {
      saveBlobResponse(await load(), fallbackName);
    } catch (err) {
      setError(await blobErrorMessage(err));
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