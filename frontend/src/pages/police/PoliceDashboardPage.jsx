import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import { policeService } from "../../services/policeService";

const TILES = [
  { key: "pendingFoundReports", label: "Pending finder reports", tone: "neutral", to: "/police/found-items/new" },
  { key: "pendingVerifications", label: "Pending item verification", tone: "caution", to: "/police/found-items" },
  { key: "verifiedFoundItems", label: "Verified items in custody", tone: "success", to: "/police/found-items" },
  { key: "pendingClaims", label: "Claims awaiting review", tone: "caution", to: "/police/claims" },
  { key: "itemsReturned", label: "Items returned to date", tone: "success", to: "/police/reports" },
  { key: "openDisputes", label: "Open disputes", tone: "danger", to: "/police/disputes" }
];

export default function PoliceDashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    policeService.getDashboard().then(setData).finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police portal"
        title="Station overview"
        actions={
          <Link to="/police/found-items/new" className="btn btn-primary btn-sm">
            Intake found item
          </Link>
        }
      />
      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="row g-3">
          {TILES.map((tile) => (
            <div className="col-md-4" key={tile.key}>
              <Link to={tile.to} className="text-decoration-none">
                <div className="card p-3 h-100">
                  <div className={`stamp stamp-${tile.tone} mb-2`} style={{ width: "fit-content" }}>
                    {tile.label}
                  </div>
                  <div className="font-mono" style={{ fontSize: "2rem", fontWeight: 600, color: "var(--ink)" }}>
                    {data[tile.key]}
                  </div>
                </div>
              </Link>
            </div>
          ))}
        </div>
      )}
    </AppLayout>
  );
}
