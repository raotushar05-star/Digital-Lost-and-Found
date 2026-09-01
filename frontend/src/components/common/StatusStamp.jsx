import React from "react";
import { getStatusMeta } from "../../utils/statusConfig";

export default function StatusStamp({ status, className = "" }) {
  const { label, tone } = getStatusMeta(status);
  return <span className={`stamp stamp-${tone} ${className}`}>{label}</span>;
}
