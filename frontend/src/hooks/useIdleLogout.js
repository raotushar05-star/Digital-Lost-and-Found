import { useEffect, useRef, useState } from "react";

export default function useIdleLogout({ enabled, idleMinutes, warnSeconds = 60, onTimeout }) {
  const [secondsLeft, setSecondsLeft] = useState(null); // null = no warning showing
  const lastActive = useRef(Date.now());
  const warning = useRef(false);
  const timeoutRef = useRef(onTimeout);
  timeoutRef.current = onTimeout;

  useEffect(() => {
    if (!enabled) return undefined;
    const idleMs = idleMinutes * 60 * 1000;
    const mark = () => { if (!warning.current) lastActive.current = Date.now(); };
    const events = ["mousemove", "keydown", "click", "scroll", "touchstart"];
    events.forEach((ev) => window.addEventListener(ev, mark, { passive: true }));
    const timer = setInterval(() => {
      const idle = Date.now() - lastActive.current;
      if (idle >= idleMs) {
        clearInterval(timer);
        timeoutRef.current();
      } else if (idle >= idleMs - warnSeconds * 1000) {
        warning.current = true;
        setSecondsLeft(Math.ceil((idleMs - idle) / 1000));
      }
    }, 1000);
    return () => {
      clearInterval(timer);
      events.forEach((ev) => window.removeEventListener(ev, mark));
    };
  }, [enabled, idleMinutes, warnSeconds]);

  const stay = () => {
    warning.current = false;
    lastActive.current = Date.now();
    setSecondsLeft(null);
  };
  return { secondsLeft, stay };
}