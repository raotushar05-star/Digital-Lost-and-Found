import React, { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import PhotoUpload from "../../components/items/PhotoUpload.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { lostItemService } from "../../services/lostItemService";
import { matchService } from "../../services/matchService";
import { formatDate } from "../../utils/format";
import { extractErrorMessage } from "../../services/apiClient";

export default function LostItemDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [item, setItem] = useState(null);
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);

  const load = () => {
    setLoading(true);
    Promise.all([lostItemService.getById(id), matchService.getMatchesForLostItem(id)])
      .then(([itemData, matchData]) => {
        setItem(itemData);
        setMatches(matchData);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, [id]);

  const handleUpload = async (file) => {
    setUploading(true);
    try {
      await lostItemService.uploadPhoto(id, file);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setUploading(false);
    }
  };

  const handleWithdraw = async () => {
    if (!window.confirm("Withdraw this lost-item report? This cannot be undone.")) return;
    try {
      await lostItemService.withdraw(id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  if (loading || !item) return <AppLayout><LoadingSpinner /></AppLayout>;

  const canEdit = item.status === "REPORTED" || item.status === "POTENTIAL_MATCH";

  return (
    <AppLayout>
      <ErrorAlert message={error} />
      <div className="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-2">
        <div>
          <div className="page-eyebrow mb-1">Lost item report</div>
          <div className="d-flex align-items-center gap-2">
            <h1 style={{ fontSize: "1.5rem" }} className="mb-0">{item.category}</h1>
            <StatusStamp status={item.status} />
          </div>
          {item.caseNumber && (
            <div className="font-mono text-muted-soft mt-1" style={{ fontSize: "0.8rem" }}>
              Case {item.caseNumber}
            </div>
          )}
        </div>
        <div className="d-flex gap-2">
          {item.caseId && (
            <Link to={`/cases/${item.caseId}`} className="btn btn-sm btn-outline-primary">
              Track case
            </Link>
          )}
          {canEdit && (
            <button className="btn btn-sm btn-outline-danger" onClick={handleWithdraw}>
              Withdraw
            </button>
          )}
        </div>
      </div>

      <div className="row g-4">
        <div className="col-lg-7">
          <div className="card p-3 mb-3">
            <p className="mb-2">{item.description}</p>
            <div className="row">
              <div className="col-6 col-md-3">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Brand</div>
                <div>{item.brand || "—"}</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Color</div>
                <div>{item.color || "—"}</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Lost date</div>
                <div>{formatDate(item.lostDate)}</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Area</div>
                <div>{item.location?.city}</div>
              </div>
            </div>
            {item.identifyingDetails && (
              <div className="mt-2">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Identifying details (private)</div>
                <div>{item.identifyingDetails}</div>
              </div>
            )}
          </div>

          <div className="card p-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">Photos</h2>
            <PhotoUpload onUpload={handleUpload} uploading={uploading} uploadedPhotos={item.photos} />
          </div>
        </div>

        <div className="col-lg-5">
          <div className="card p-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">Potential matches</h2>
            {matches.length === 0 ? (
              <p className="text-muted-soft mb-0" style={{ fontSize: "0.85rem" }}>
                No matches yet. We'll notify you as soon as a likely candidate is verified by police.
              </p>
            ) : (
              <div className="list-group list-group-flush">
                {matches.map((m) => (
                  <Link
                    key={m.matchId}
                    to={`/found-items/${m.foundItemId}`}
                    className="list-group-item list-group-item-action px-0"
                  >
                    <div className="d-flex justify-content-between">
                      <span className="fw-medium">{m.foundItemCategory}</span>
                      <span className="font-mono text-brass" style={{ fontSize: "0.78rem" }}>
                        {Number(m.matchScore).toFixed(0)}%
                      </span>
                    </div>
                    <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>{m.matchReason}</div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
