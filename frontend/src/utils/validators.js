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
export const NAME_RE = /^[\p{L}][\p{L} .'-]{1,99}$/u;
export const EMAIL_RE =
  /^(?!.*\.\.)[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\.[A-Za-z]{2,}$/;
export const PHONE_RE = /^[0-9]{10}$/;
export const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;
const BRAND_RE = /^[\p{L}\p{N}][\p{L}\p{N} .&'/-]{0,99}$/u;
const COLOR_RE = /^[\p{L}][\p{L} /,-]{0,49}$/u;
const CITY_RE = /^[\p{L}][\p{L} .'-]{1,99}$/u;

/** Returns the first problem with a lost/found item form, or null. */
export function itemError(f) {
  const desc = (f.description || "").trim();
  if (desc.length < 5 || desc.length > 1000) return "Description must be 5 to 1000 characters.";
  if (f.brand && !BRAND_RE.test(f.brand.trim())) return "Brand: letters, numbers and spaces only (max 100).";
  if (f.color && !COLOR_RE.test(f.color.trim())) return "Colour: letters and spaces only (max 50).";
  if (f.identifyingDetails && f.identifyingDetails.length > 1000) return "Identifying details: max 1000 characters.";
  if (f.city !== undefined && !CITY_RE.test((f.city || "").trim())) return "City: letters and spaces only.";
  return null;
}