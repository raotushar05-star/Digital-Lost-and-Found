import React, { useEffect, useState } from "react";
import apiClient from "../../services/apiClient";

export default function ProtectedImage({ fileUrl, alt = "", style, className }) {
  const [src, setSrc] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let active = true;
    let objectUrl = null;
    setSrc(null);
    setFailed(false);
    apiClient
      .get(fileUrl.replace(/^.*\/api\/v1/, ""), { responseType: "blob" })
      .then((res) => {
        if (!active) return;
        objectUrl = URL.createObjectURL(res.data);
        setSrc(objectUrl);
      })
      .catch(() => active && setFailed(true));
    return () => {
      active = false;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [fileUrl]);

  if (failed) {
    return (
      <div style={style} className={`d-flex align-items-center justify-content-center text-muted-soft ${className || ""}`}>
        <span style={{ fontSize: "0.7rem" }}>Unavailable</span>
      </div>
    );
  }
  if (!src) return <div style={{ ...style, background: "var(--paper)" }} className={className} />;
  return <img src={src} alt={alt} style={style} className={className} />;
}