import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { useAuth } from "../../context/AuthContext.jsx";
import { userService } from "../../services/userService";
import { matchService } from "../../services/matchService";
import { formatDate } from "../../utils/format";

export default function DashboardPage() {
  const { user } = useAuth();
  const [lostItems, setLostItems] = useState([]);
  const [claims, setClaims] = useState([]);
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([userService.getMyLostItems(), userService.getMyClaims(), matchService.getMyMatches()])
      .then(([li, cl, mt]) => {
        setLostItems(li);
        setClaims(cl);
        setMatches(mt);
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Citizen dashboard"
        title={`Welcome, ${user?.name?.split(" ")[0] || "there"}`}
        actions={
          <>
            <Link to="/lost-items/report" className="btn btn-primary btn-sm">
              Report lost item
            </Link>
            <Link to="/found-items/report" className="btn btn-outline-primary btn-sm">
              Report found item
            </Link>
          </>
        }
      />

      {loading ? (
        <LoadingSpinner />
      ) : (
        <div className="row g-4">
          <div className="col-lg-7">
            <div className="card p-3 mb-4">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <h2 style={{ fontSize: "1.05rem" }} className="mb-0">
                  Your lost-item reports
                </h2>
                <span className="text-muted-soft font-mono" style={{ fontSize: "0.75rem" }}>
                  {lostItems.length} total
                </span>
              </div>
              {lostItems.length === 0 ? (
                <EmptyState title="Nothing reported yet" message="Lost something? File a report to get matched." />
              ) : (
                <div className="list-group list-group-flush">
                  {lostItems.slice(0, 6).map((item) => (
                    <Link
                      key={item.lostItemId}
                      to={`/lost-items/${item.lostItemId}`}
                      className="list-group-item list-group-item-action d-flex justify-content-between align-items-center px-0"
                    >
                      <div>
                        <div className="fw-medium">{item.category}</div>
                        <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                          {item.description?.slice(0, 60)} · Lost {formatDate(item.lostDate)}
                        </div>
                      </div>
                      <StatusStamp status={item.status} />
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <div className="card p-3">
              <h2 style={{ fontSize: "1.05rem" }} className="mb-2">
                Your claims
              </h2>
              {claims.length === 0 ? (
                <EmptyState title="No claims yet" message="Found a match? Submit a claim from the item's page." />
              ) : (
                <div className="list-group list-group-flush">
                  {claims.slice(0, 6).map((claim) => (
                    <Link
                      key={claim.claimId}
                      to={`/claims/${claim.claimId}`}
                      className="list-group-item list-group-item-action d-flex justify-content-between align-items-center px-0"
                    >
                      <div>
                        <div className="fw-medium">{claim.foundItemCategory}</div>
                        <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                          {claim.foundItemDescription?.slice(0, 60)}
                        </div>
                      </div>
                      <StatusStamp status={claim.status} />
                    </Link>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="col-lg-5">
            <div className="card p-3">
              <div className="d-flex align-items-center gap-2 mb-2">
                <h2 style={{ fontSize: "1.05rem" }} className="mb-0">
                  Potential matches
                </h2>
                <span className="stamp stamp-caution">{matches.length}</span>
              </div>
              {matches.length === 0 ? (
                <EmptyState title="No matches yet" message="We'll notify you the moment a likely match appears." />
              ) : (
                <div className="list-group list-group-flush">
                  {matches.slice(0, 8).map((m) => (
                    <Link
                      key={m.matchId}
                      to={`/found-items/${m.foundItemId}`}
                      className="list-group-item list-group-item-action px-0"
                    >
                      <div className="d-flex justify-content-between">
                        <div className="fw-medium">{m.foundItemCategory}</div>
                        <span className="font-mono text-brass" style={{ fontSize: "0.78rem" }}>
                          {Number(m.matchScore).toFixed(0)}% match
                        </span>
                      </div>
                      <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                        {m.foundItemDescription?.slice(0, 60)} · {m.foundItemCity}
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
