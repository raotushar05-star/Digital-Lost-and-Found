import React, { useState } from "react";
import apiClient from "../../services/apiClient";

/** Opens a login-protected file. Plain links cannot send the login token, so we fetch it first. */
export default function ProtectedFileLink({ fileUrl, label = "View file" }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const open = async (e) => {
    e.preventDefault();
    const win = window.open("", "_blank"); // opened first so the popup is not blocked
    setBusy(true);
    setError(null);
    try {
      const path = fileUrl.replace(/^.*\/api\/v1/, "");
      const res = await apiClient.get(path, { responseType: "blob" });
      const url = URL.createObjectURL(res.data);
      if (win) win.location.href = url;
      else window.location.href = url;
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch {
      if (win) win.close();
      setError("Could not open this file.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <span>
      <a href={fileUrl} onClick={open} style={{ fontSize: "0.8rem" }}>{busy ? "Opening…" : label}</a>
      {error && <span className="text-danger ms-2" style={{ fontSize: "0.72rem" }}>{error}</span>}
    </span>
  );
}