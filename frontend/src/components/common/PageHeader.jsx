import React from "react";

export default function PageHeader({ eyebrow, title, subtitle, actions }) {
  return (
    <div className="d-flex flex-wrap justify-content-between align-items-start mb-4 gap-3">
      <div>
        {eyebrow && <div className="page-eyebrow mb-1">{eyebrow}</div>}
        <h1 className="mb-1" style={{ fontSize: "1.6rem" }}>{title}</h1>
        {subtitle && <p className="text-muted mb-0">{subtitle}</p>}
      </div>
      {actions && <div className="d-flex gap-2">{actions}</div>}
    </div>
  );
}
