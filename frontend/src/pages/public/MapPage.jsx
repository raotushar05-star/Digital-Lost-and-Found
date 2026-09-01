import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ResultsMap from "../../components/maps/ResultsMap.jsx";
import { searchService } from "../../services/searchService";

const DEFAULT_CENTER = { latitude: 12.9716, longitude: 77.5946 };

export default function MapPage() {
  const [items, setItems] = useState([]);
  const [radius, setRadius] = useState(10);
  const [loading, setLoading] = useState(true);
  const [center, setCenter] = useState(DEFAULT_CENTER);

  const load = (loc, r) => {
    setLoading(true);
    searchService
      .nearby({ latitude: loc.latitude, longitude: loc.longitude, radius: r })
      .then(setItems)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const loc = { latitude: pos.coords.latitude, longitude: pos.coords.longitude };
          setCenter(loc);
          load(loc, radius);
        },
        () => load(DEFAULT_CENTER, radius)
      );
    } else {
      load(DEFAULT_CENTER, radius);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Map view"
        title="Found items near you"
        subtitle="Verified, in-custody items within the selected radius."
        actions={
          <select
            className="form-select form-select-sm"
            style={{ width: 140 }}
            value={radius}
            onChange={(e) => {
              const r = Number(e.target.value);
              setRadius(r);
              load(center, r);
            }}
          >
            <option value={2}>2 km</option>
            <option value={5}>5 km</option>
            <option value={10}>10 km</option>
            <option value={25}>25 km</option>
            <option value={50}>50 km</option>
          </select>
        }
      />
      {loading ? <LoadingSpinner /> : <ResultsMap items={items} />}
    </AppLayout>
  );
}
