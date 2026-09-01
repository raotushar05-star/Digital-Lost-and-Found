import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { userService } from "../../services/userService";

export default function MyClaimsPage() {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    userService.getMyClaims().then(setClaims).finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <PageHeader eyebrow="Citizen" title="My claims" />
      {loading ? (
        <LoadingSpinner />
      ) : claims.length === 0 ? (
        <EmptyState
          title="No claims yet"
          message="When you find an item that's yours in search results, submit a claim from its page."
          action={
            <Link to="/search" className="btn btn-primary btn-sm">
              Search found items
            </Link>
          }
        />
      ) : (
        <div className="card">
          <div className="list-group list-group-flush">
            {claims.map((claim) => (
              <Link
                key={claim.claimId}
                to={`/claims/${claim.claimId}`}
                className="list-group-item list-group-item-action d-flex justify-content-between align-items-center p-3"
              >
                <div>
                  <div className="fw-medium">{claim.foundItemCategory}</div>
                  <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                    {claim.foundItemDescription?.slice(0, 80)}
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
