import React, { useEffect, useRef, useState } from "react";

export default function FlashAlert({ message, variant = "danger" }) {
  const ref = useRef(null);
  const [dismissed, setDismissed] = useState(false);

  useEffect(() => {
    setDismissed(false);
    if (message && ref.current) {
      ref.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [message]);

  if (!message || dismissed) return null;
  return (
    <div
      ref={ref}
      className={`alert alert-${variant} alert-dismissible`}
      role={variant === "danger" ? "alert" : "status"}
      style={{ borderRadius: "var(--radius-sm)", scrollMarginTop: 90 }}
    >
      {message}
      <button type="button" className="btn-close" aria-label="Dismiss" onClick={() => setDismissed(true)} />
    </div>
  );
}