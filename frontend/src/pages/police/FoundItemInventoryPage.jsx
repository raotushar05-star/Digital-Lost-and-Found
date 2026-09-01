import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { policeService } from "../../services/policeService";
import { stationService } from "../../services/stationService";
import { useAuth } from "../../context/AuthContext.jsx";
import { formatDate } from "../../utils/format";

export default function FoundItemInventoryPage() {
  const { profile, isAdmin } = useAuth();
  const [stations, setStations] = useState([]);
  const [stationId, setStationId] = useState("");
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");

  useEffect(() => {
    if (isAdmin) {
      stationService.getStations().then(setStations).catch(() => {});
    }
  }, [isAdmin]);

  useEffect(() => {
    if (profile?.stationId) setStationId(profile.stationId);
  }, [profile]);

  useEffect(() => {
    if (!stationId) return;
    setLoading(true);
    policeService.getStationInventory(stationId).then(setItems).finally(() => setLoading(false));
  }, [stationId]);

  const filtered = items.filter((i) => filter === "ALL" || i.verificationStatus === filter);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Custody"
        title="Found item inventory"
        actions={
          <>
            {isAdmin && stations.length > 0 && (
              <select className="form-select form-select-sm" style={{ width: 200 }} value={stationId} onChange={(e) => setStationId(e.target.value)}>
                {stations.map((s) => (
                  <option key={s.stationId} value={s.stationId}>{s.stationName}</option>
                ))}
              </select>
            )}
            <select className="form-select form-select-sm" style={{ width: 160 }} value={filter} onChange={(e) => setFilter(e.target.value)}>
              <option value="ALL">All statuses</option>
              <option value="PENDING">Pending verification</option>
              <option value="VERIFIED">Verified</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </>
        }
      />
      {loading ? (
        <LoadingSpinner />
      ) : filtered.length === 0 ? (
        <EmptyState title="No items found" message="Nothing matches this filter at this station yet." />
      ) : (
        <div className="card">
          <div className="list-group list-group-flush">
            {filtered.map((item) => (
              <Link
                key={item.foundItemId}
                to={`/police/found-items/${item.foundItemId}`}
                className="list-group-item list-group-item-action d-flex justify-content-between align-items-center p-3"
              >
                <div>
                  <div className="fw-medium">{item.category}</div>
                  <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                    {item.description?.slice(0, 70)} · Found {formatDate(item.foundDate)}
                  </div>
                </div>
                <div className="d-flex gap-2">
                  <StatusStamp status={item.custodyStatus} />
                  <StatusStamp status={item.verificationStatus} />
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}
    </AppLayout>
  );
}
