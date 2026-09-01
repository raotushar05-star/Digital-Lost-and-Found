import React, { useMemo } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import { Link } from "react-router-dom";
import "./leafletSetup";

const DEFAULT_CENTER = [12.9716, 77.5946];

export default function ResultsMap({ items = [], height = 420 }) {
  const center = useMemo(() => {
    const withLoc = items.find((i) => i.location && i.location.latitude);
    if (withLoc) return [Number(withLoc.location.latitude), Number(withLoc.location.longitude)];
    return DEFAULT_CENTER;
  }, [items]);

  return (
    <div style={{ height, borderRadius: "var(--radius)", overflow: "hidden", border: "1px solid var(--line)" }}>
      <MapContainer center={center} zoom={12} style={{ height: "100%", width: "100%" }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {items
          .filter((item) => item.location && item.location.latitude)
          .map((item) => (
            <Marker key={item.foundItemId} position={[Number(item.location.latitude), Number(item.location.longitude)]}>
              <Popup>
                <div style={{ minWidth: 160 }}>
                  <strong>{item.category}</strong>
                  <div className="text-muted" style={{ fontSize: "0.82rem" }}>
                    {item.description?.slice(0, 80)}
                  </div>
                  <Link to={`/found-items/${item.foundItemId}`} className="d-block mt-1">
                    View details →
                  </Link>
                </div>
              </Popup>
            </Marker>
          ))}
      </MapContainer>
    </div>
  );
}
