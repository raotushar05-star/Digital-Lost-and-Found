import React, { useEffect, useRef } from "react";

const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

export const googleEnabled = Boolean(CLIENT_ID);

/** Reads name/email from the token for pre-filling only. The backend does the real verification. */
export function decodeGoogleCredential(token) {
  const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
  const json = decodeURIComponent(
    atob(payload)
      .split("")
      .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
      .join("")
  );
  return JSON.parse(json);
}

export default function GoogleVerifyButton({ onCredential }) {
  const holder = useRef(null);

  useEffect(() => {
    let tries = 0;
    const timer = setInterval(() => {
      tries += 1;
      if (window.google?.accounts?.id) {
        clearInterval(timer);
        window.google.accounts.id.initialize({
          client_id: CLIENT_ID,
          callback: (response) => onCredential(response.credential)
        });
        window.google.accounts.id.renderButton(holder.current, {
          theme: "outline",
          size: "large",
          text: "continue_with",
          width: 340
        });
      } else if (tries > 50) {
        clearInterval(timer);
      }
    }, 100);
    return () => clearInterval(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <div ref={holder} className="d-flex justify-content-center" />;
}