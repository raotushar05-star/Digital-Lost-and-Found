import React, { useCallback, useMemo, useRef, useState } from "react";
import { MapContainer, TileLayer, Marker, useMap, useMapEvents } from "react-leaflet";
import "./leafletSetup";

const DEFAULT_CENTER = [12.9716, 77.5946]; // Bengaluru, used as a sensible fallback

function ClickHandler({ onPick }) {
  useMapEvents({
    click(e) {
      onPick(e.latlng.lat, e.latlng.lng);
    }
  });
  return null;
}

// react-leaflet only reads MapContainer's `center` prop on the very first
// render - changing it later does nothing. To recenter programmatically
// (e.g. after geolocation resolves), we grab the underlying Leaflet map
// instance via useMap() and stash it in a ref the parent can call directly.
function MapRefBinder({ mapRef }) {
  const map = useMap();
  mapRef.current = map;
  return null;
}

/**
 * Map + Location Module: lets a citizen or officer pick the approximate
 * lost/found location by clicking the map, or by using their current
 * device location. Emits { latitude, longitude } via onChange.
 */
export default function LocationPicker({ value, onChange, height = 320 }) {
  const [locating, setLocating] = useState(false);
  const mapRef = useRef(null);
  const center = useMemo(() => {
    if (value && value.latitude && value.longitude) {
      return [Number(value.latitude), Number(value.longitude)];
    }
    return DEFAULT_CENTER;
  }, [value]);

  const handlePick = useCallback(
    (lat, lng) => {
      onChange({ ...value, latitude: lat, longitude: lng });
    },
    [onChange, value]
  );

  const useMyLocation = () => {
    if (!navigator.geolocation) return;
    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        handlePick(latitude, longitude);
        // Manual map clicks don't need a recenter (the clicked point is
        // already on screen); geolocation can jump far outside the current
        // view, so recenter explicitly here.
        if (mapRef.current) {
          mapRef.current.setView([latitude, longitude], 15, { animate: true });
        }
        setLocating(false);
      },
      () => setLocating(false),
      { enableHighAccuracy: true, timeout: 8000 }
    );
  };

  const hasPin = value && value.latitude && value.longitude;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-2">
        <span className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
          Click the map to drop a pin at the approximate location.
        </span>
        <button type="button" className="btn btn-sm btn-outline-primary" onClick={useMyLocation} disabled={locating}>
          {locating ? "Locating…" : "Use my location"}
        </button>
      </div>
      <div style={{ height, borderRadius: "var(--radius)", overflow: "hidden", border: "1px solid var(--line)" }}>
        <MapContainer center={center} zoom={hasPin ? 14 : 12} style={{ height: "100%", width: "100%" }}>
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <ClickHandler onPick={handlePick} />
          <MapRefBinder mapRef={mapRef} />
          {hasPin && <Marker position={[Number(value.latitude), Number(value.longitude)]} />}
        </MapContainer>
      </div>
      {hasPin && (
        <div className="font-mono text-muted-soft mt-1" style={{ fontSize: "0.75rem" }}>
          {Number(value.latitude).toFixed(6)}, {Number(value.longitude).toFixed(6)}
        </div>
      )}
    </div>
  );
}
