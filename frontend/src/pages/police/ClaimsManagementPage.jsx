import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { policeService } from "../../services/policeService";
import { useAuth } from "../../context/AuthContext.jsx";

export default function ClaimsManagementPage() {
  const { profile } = useAuth();
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");

  useEffect(() => {
    if (!profile?.stationId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    policeService
      .getStationInventory(profile.stationId)
      .then(async (items) => {
        const perItem = await Promise.all(
          items.map((item) =>
            policeService
              .getClaimsForFoundItem(item.foundItemId)
              .then((claimList) => claimList.map((c) => ({ ...c, itemCategory: item.category })))
              .catch(() => [])
          )
        );
        setClaims(perItem.flat().sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
      })
      .finally(() => setLoading(false));
  }, [profile]);

  const filtered = claims.filter((c) => filter === "ALL" || c.status === filter);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Claims"
        title="Claims management"
        subtitle="Every ownership claim submitted against items in your station's custody."
        actions={
          <select className="form-select form-select-sm" style={{ width: 200 }} value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="ALL">All statuses</option>
            <option value="PENDING">Pending</option>
            <option value="UNDER_VERIFICATION">Under verification</option>
            <option value="DISPUTED">Disputed</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        }
      />
      {loading ? (
        <LoadingSpinner />
      ) : filtered.length === 0 ? (
        <EmptyState title="No claims to review" message="Claims will appear here as citizens submit them." />
      ) : (
        <div className="card">
          <div className="list-group list-group-flush">
            {filtered.map((claim) => (
              <Link
                key={claim.claimId}
                to={`/police/claims/${claim.claimId}`}
                className="list-group-item list-group-item-action d-flex justify-content-between align-items-center p-3"
              >
                <div>
                  <div className="fw-medium">{claim.claimantName} — {claim.foundItemCategory || claim.itemCategory}</div>
                  <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                    {claim.foundItemDescription?.slice(0, 70)}
                  </div>
                </div>
                <StatusStamp status={claim.status} />
              </Link>
            ))}
          </div>
        </div>
      )}
    </AppLayout>
  );
}
