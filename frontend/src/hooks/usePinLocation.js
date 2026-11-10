import { useRef, useState } from "react";
import { reverseGeocode } from "../utils/geocode";

export function cleanLocation(loc) {
  return {
    latitude: loc.latitude,
    longitude: loc.longitude,
    city: loc.city,
    state: loc.state || undefined,
    postalCode: loc.postalCode || undefined,
    locality: loc.locality || undefined
  };
}

export default function usePinLocation() {
  const [location, setLocation] = useState({ city: "" });
  const [geo, setGeo] = useState({ status: "idle" });
  const request = useRef(0);

  const handlePin = async (picked) => {
    const id = ++request.current;
    setLocation({ latitude: picked.latitude, longitude: picked.longitude, city: "", state: "", postalCode: "", locality: "", _country: null });
    setGeo({ status: "loading" });
    try {
      const place = await reverseGeocode(picked.latitude, picked.longitude);
      if (id !== request.current) return; // a newer click replaced this one
      setLocation((l) => ({ ...l, city: place.city, state: place.state, postalCode: place.postalCode, locality: place.locality, _country: place.country }));
      setGeo({ status: "ok" });
    } catch {
      if (id !== request.current) return;
      setGeo({ status: "error" });
    }
  };

  /** Returns a message to show, or null when the pin is fine. */
  const pinProblem = () => {
    if (location.latitude == null || location.longitude == null) return "Please click the map to mark the place.";
    if (geo.status === "loading") return "Still looking up the area, please wait a moment.";
    if (geo.status === "error") return "Could not identify this place. Click a different point on the map.";
    if (location._country && location._country !== "in") return "The pin must be inside India.";
    if (!location.city) return "No town or city found at this point. Click a nearby point.";
    return null;
  };

  return { location, geo, handlePin, pinProblem };
}