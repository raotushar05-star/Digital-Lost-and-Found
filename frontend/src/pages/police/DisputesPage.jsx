import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { policeService } from "../../services/policeService";
import { useAuth } from "../../context/AuthContext.jsx";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDateTime } from "../../utils/format";

export default function DisputesPage() {
  const { profile } = useAuth();
  const [disputes, setDisputes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [resolutionDrafts, setResolutionDrafts] = useState({});

  const load = () => {
    if (!profile?.stationId) {
      setLoading(false);
      return;
    }
    setLoading(true);
    policeService
      .getStationInventory(profile.stationId)
      .then(async (items) => {
        const perItem = await Promise.all(
          items.map((item) => policeService.getDisputes(item.foundItemId).catch(() => []))
        );
        setDisputes(perItem.flat().sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, [profile]);

  const handleResolve = async (disputeId, status) => {
    setError(null);
    try {
      await policeService.updateDispute(disputeId, {
        status,
        resolution: resolutionDrafts[disputeId] || ""
      });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Fraud prevention"
        title="Disputes"
        subtitle="Competing or suspicious claims flagged for closer review."
      />
      <ErrorAlert message={error} />
      {loading ? (
        <LoadingSpinner />
      ) : disputes.length === 0 ? (
        <EmptyState title="No disputes" message="Raise a dispute from a claim's review page if evidence conflicts." />
      ) : (
        <div className="d-flex flex-column gap-3">
          {disputes.map((d) => (
            <div className="card p-3" key={d.disputeId}>
              <div className="d-flex justify-content-between align-items-start">
                <div>
                  <div className="fw-medium">Dispute on claim by {d.raisedByName ? `raised by ${d.raisedByName}` : ""}</div>
                  <p className="mb-1">{d.reason}</p>
                  <div className="text-muted-soft font-mono" style={{ fontSize: "0.75rem" }}>
                    {formatDateTime(d.createdAt)}
                  </div>
                </div>
                <StatusStamp status={d.status} />
              </div>
              <Link to={`/police/claims/${d.claimId}`} className="d-inline-block mt-2" style={{ fontSize: "0.85rem" }}>
                View the disputed claim →
              </Link>

              {(d.status === "OPEN" || d.status === "UNDER_REVIEW") && (
                <div className="hairline-top pt-2 mt-2">
                  <textarea
                    className="form-control form-control-sm mb-2"
                    rows={2}
                    placeholder="Resolution notes"
                    value={resolutionDrafts[d.disputeId] || ""}
                    onChange={(e) => setResolutionDrafts({ ...resolutionDrafts, [d.disputeId]: e.target.value })}
                  />
                  <div className="d-flex gap-2">
                    {d.status === "OPEN" && (
                      <button className="btn btn-sm btn-outline-secondary" onClick={() => handleResolve(d.disputeId, "UNDER_REVIEW")}>
                        Mark under review
                      </button>
                    )}
                    <button className="btn btn-sm btn-primary" onClick={() => handleResolve(d.disputeId, "RESOLVED")}>
                      Resolve
                    </button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleResolve(d.disputeId, "CLOSED")}>
                      Close
                    </button>
                  </div>
                </div>
              )}
              {d.resolution && (
                <div className="text-muted-soft mt-2" style={{ fontSize: "0.85rem" }}>
                  Resolution: {d.resolution}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </AppLayout>
  );
}
