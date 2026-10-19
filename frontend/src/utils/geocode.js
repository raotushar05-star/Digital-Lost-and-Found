/** Looks up the real place at a map pin using OpenStreetMap Nominatim (free, about 1 request per second). */
export async function reverseGeocode(lat, lng) {
  const url =
    "https://nominatim.openstreetmap.org/reverse?format=jsonv2&addressdetails=1&zoom=14&accept-language=en" +
    `&lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lng)}`;
  const res = await fetch(url, { headers: { Accept: "application/json" } });
  if (!res.ok) throw new Error("Lookup failed");
  const data = await res.json();
  const a = data.address;
  if (!a) throw new Error("No place found");
  const city =
    a.city || a.town || a.village || a.municipality || a.city_district || a.county || a.state_district || "";
  return {
    city,
    state: a.state || "",
    postalCode: (a.postcode || "").replace(/\s/g, "").slice(0, 10),
    locality: a.suburb || a.neighbourhood || a.hamlet || "",
    country: (a.country_code || "").toLowerCase(),
    displayName: data.display_name || ""
  };
}