import React from "react";

export default function EmptyState({ title, message, action }) {
  return (
    <div className="empty-state">
      <h5 className="font-display mb-2">{title}</h5>
      {message && <p className="mb-3">{message}</p>}
      {action}
    </div>
  );
}
