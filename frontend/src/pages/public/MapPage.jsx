import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import ResultsMap from "../../components/maps/ResultsMap.jsx";
import { useAuth } from "../../context/AuthContext.jsx";
import { searchService } from "../../services/searchService";
import { userService } from "../../services/userService";

const DEFAULT_CITY = "Mangaluru";
const DEFAULT_CENTER = { latitude: 12.9141, longitude: 74.8560 };

export default function MapPage() {
  const { isAuthenticated, user } = useAuth();
  const [items, setItems] = useState([]);
  const [radius, setRadius] = useState(10);
  const [loading, setLoading] = useState(true);
  const [center, setCenter] = useState(DEFAULT_CENTER);
  const [city, setCity] = useState(DEFAULT_CITY);
  const [error, setError] = useState(null);

  const loadCity = (cityName) => {
    setLoading(true);
    setError(null);
    Promise.all([
      searchService.search({ city: cityName.trim(), page: 0, size: 100 }),
      isAuthenticated && user?.role === "USER"
        ? Promise.all([userService.getMyLostItems(), userService.getMyFoundReports()])
        : Promise.resolve([[], []])
    ])
      .then(([response, [lostItems, foundReports]]) => {
        const cityFilter = cityName.trim().toLocaleLowerCase();
        const publicItems = (response.content || []).map((item) => ({
          ...item,
          mapKey: `found-${item.foundItemId}`,
          mapType: "Verified found item"
        }));
        const myLostItems = lostItems
          .filter((item) => !["RESOLVED", "RETURNED", "WITHDRAWN"].includes(item.status))
          .filter((item) => item.city?.toLocaleLowerCase().includes(cityFilter))
          .map((item) => ({
            ...item,
            mapKey: `lost-${item.lostItemId}`,
            mapType: "My lost-item report",
            location: { latitude: item.latitude, longitude: item.longitude, city: item.city }
          }));
        const myFoundReports = foundReports
          .filter((report) => report.status !== "RETURNED")
          .filter((report) => report.location?.city?.toLocaleLowerCase().includes(cityFilter))
          .map((report) => ({
            ...report,
            mapKey: `report-${report.foundReportId}`,
            mapType: "My found-item report"
          }));
        setItems([...publicItems, ...myLostItems, ...myFoundReports]);
      })
      .catch(() => setError("Could not load found items for that city. Please try again."))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadCity(DEFAULT_CITY);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const useMyLocation = () => {
    if (!navigator.geolocation) {
      setError("Location is not available in this browser. Search by city instead.");
      return;
    }
    setLoading(true);
    setError(null);
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        const location = { latitude: coords.latitude, longitude: coords.longitude };
        setCenter(location);
        searchService.nearby({ ...location, radius })
          .then(setItems)
          .catch(() => setError("Could not load nearby found items. Please try again."))
          .finally(() => setLoading(false));
      },
      () => {
        setError("Could not access your location. Search by city instead.");
        setLoading(false);
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Map view"
        title="Found items on the map"
        subtitle="Verified found items and your own reports. Search a city or use your location."
        actions={
          <div className="d-flex flex-wrap align-items-end gap-2">
            <form
              className="d-flex align-items-end gap-2"
              onSubmit={(event) => {
                event.preventDefault();
                if (city.trim()) loadCity(city);
              }}
            >
              <div>
                <label htmlFor="map-city" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>City</label>
                <input id="map-city" className="form-control form-control-sm" value={city} onChange={(event) => setCity(event.target.value)} />
              </div>
              <button className="btn btn-sm btn-outline-primary" disabled={!city.trim() || loading}>Search</button>
            </form>
            <div>
              <label htmlFor="map-radius" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Nearby radius</label>
              <select id="map-radius" className="form-select form-select-sm" value={radius} onChange={(event) => setRadius(Number(event.target.value))}>
                <option value={2}>2 km</option>
                <option value={5}>5 km</option>
                <option value={10}>10 km</option>
                <option value={25}>25 km</option>
                <option value={50}>50 km</option>
              </select>
            </div>
            <button className="btn btn-sm btn-outline-primary" onClick={useMyLocation} disabled={loading}>Use my location</button>
          </div>
        }
      />
      {error && <ErrorAlert message={error} />}
      {loading ? <LoadingSpinner /> : items.length === 0 ? (
        <EmptyState
          title="No locations to show"
          message={isAuthenticated && user?.role === "USER"
            ? "No verified found items or your own active reports with locations were found in this city."
            : "Only police-verified found items still in custody appear publicly on this map."}
        />
      ) : (
        <ResultsMap items={items} center={center} />
      )}
    </AppLayout>
  );
}
