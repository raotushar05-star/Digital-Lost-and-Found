const ADDRESS_RE = /^(?=(?:[\s\S]*\p{L}){3})[\p{L}\p{N}\s,.\-/#'()&]{10,250}$/u;

export const cleanAddress = (v) => (v || "").replace(/\s+/g, " ").trim();

/** Returns an error message, or null when the address is fine. */
export function addressError(value, { required = true } = {}) {
  const v = cleanAddress(value);
  if (!v) return required ? "Address is required." : null;
  if (v.length < 10) return "Address is too short (at least 10 characters).";
  if (v.length > 250) return "Address is too long (at most 250 characters).";
  if (!ADDRESS_RE.test(v)) {
    return "Use at least 3 letters, and only letters, numbers, spaces and , . - / # ' ( ) &";
  }
  return null;
}