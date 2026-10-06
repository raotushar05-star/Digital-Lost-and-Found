import React, { useEffect, useMemo } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import { Link } from "react-router-dom";
import "./leafletSetup";

function FitItems({ locations }) {
  const map = useMap();

  useEffect(() => {
    if (locations.length === 1) {
      map.setView(locations[0], 14);
    } else if (locations.length > 1) {
      map.fitBounds(locations, { padding: [32, 32], maxZoom: 14 });
    }
  }, [locations, map]);

  return null;
}

export default function ResultsMap({ items = [], center, height = 420 }) {
  const locations = useMemo(() => items
    .filter((item) => item.location && item.location.latitude != null && item.location.longitude != null)
    .map((item) => [Number(item.location.latitude), Number(item.location.longitude)])
    .filter(([latitude, longitude]) => Number.isFinite(latitude) && Number.isFinite(longitude)), [items]);
  const initialCenter = locations[0] || [center.latitude, center.longitude];

  return (
    <div style={{ height, borderRadius: "var(--radius)", overflow: "hidden", border: "1px solid var(--line)" }}>
      <MapContainer center={initialCenter} zoom={12} style={{ height: "100%", width: "100%" }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <FitItems locations={locations} />
        {items
          .filter((item) => item.location && item.location.latitude != null && item.location.longitude != null)
          .map((item) => (
            <Marker key={item.mapKey || item.foundItemId} position={[Number(item.location.latitude), Number(item.location.longitude)]}>
              <Popup>
                <div style={{ minWidth: 160 }}>
                  <strong>{item.mapType || "Verified found item"} · {item.category}</strong>
                  <div className="text-muted" style={{ fontSize: "0.82rem" }}>
                    {item.description?.slice(0, 80)}
                  </div>
                  {item.foundItemId && (
                    <Link to={`/found-items/${item.foundItemId}`} className="d-block mt-1">
                      View details →
                    </Link>
                  )}
                  {item.lostItemId && (
                    <Link to={`/lost-items/${item.lostItemId}`} className="d-block mt-1">
                      View report →
                    </Link>
                  )}
                </div>
              </Popup>
            </Marker>
          ))}
      </MapContainer>
    </div>
  );
}
