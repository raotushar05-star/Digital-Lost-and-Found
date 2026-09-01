import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import PhotoUpload from "../../components/items/PhotoUpload.jsx";
import { policeService } from "../../services/policeService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDate, formatDateTime } from "../../utils/format";

export default function FoundItemAdminDetailPage() {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState(null);
  const [verifying, setVerifying] = useState(false);
  const [notes, setNotes] = useState("");

  const load = () => {
    setLoading(true);
    Promise.all([policeService.getFoundItemDetail(id), policeService.getClaimsForFoundItem(id)])
      .then(([itemData, claimData]) => {
        setItem(itemData);
        setClaims(claimData);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, [id]);

  const handleUpload = async (file) => {
    setUploading(true);
    try {
      await policeService.uploadFoundItemPhoto(id, file, "PUBLIC");
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setUploading(false);
    }
  };

  const handleVerify = async (decision) => {
    setVerifying(true);
    setError(null);
    try {
      await policeService.verifyFoundItem(id, { decision, verificationNotes: notes });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setVerifying(false);
    }
  };

  if (loading || !item) return <AppLayout><LoadingSpinner /></AppLayout>;

  return (
    <AppLayout>
      <ErrorAlert message={error} />
      <div className="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-2">
        <div>
          <div className="page-eyebrow mb-1">Found item · Police record</div>
          <div className="d-flex align-items-center gap-2 flex-wrap">
            <h1 style={{ fontSize: "1.5rem" }} className="mb-0">{item.category}</h1>
            <StatusStamp status={item.verificationStatus} />
            <StatusStamp status={item.custodyStatus} />
          </div>
          {item.caseNumber && (
            <div className="font-mono text-muted-soft mt-1" style={{ fontSize: "0.8rem" }}>
              Case {item.caseNumber}
            </div>
          )}
        </div>
        {item.caseId && (
          <Link to={`/cases/${item.caseId}`} className="btn btn-sm btn-outline-primary">
            Track case
          </Link>
        )}
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
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Found date</div>
                <div>{formatDate(item.foundDate)}</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Received</div>
                <div>{formatDateTime(item.receivedDate)}</div>
              </div>
            </div>
            {item.privateIdentifyingDetails && (
              <div className="mt-2 p-2" style={{ background: "var(--brass-tint)", borderRadius: "var(--radius-sm)" }}>
                <div className="text-brass" style={{ fontSize: "0.72rem", fontWeight: 600 }}>RESTRICTED — INTERNAL ONLY</div>
                <div style={{ fontSize: "0.9rem" }}>{item.privateIdentifyingDetails}</div>
              </div>
            )}
          </div>

          {item.verificationStatus === "PENDING" && (
            <div className="card p-3 mb-3">
              <h2 style={{ fontSize: "1rem" }} className="mb-2">Verification decision</h2>
              <textarea
                className="form-control mb-2"
                rows={2}
                placeholder="Verification notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
              />
              <div className="d-flex gap-2">
                <button className="btn btn-primary btn-sm" disabled={verifying} onClick={() => handleVerify("VERIFIED")}>
                  Verify item
                </button>
                <button className="btn btn-outline-danger btn-sm" disabled={verifying} onClick={() => handleVerify("REJECTED")}>
                  Reject
                </button>
              </div>
            </div>
          )}

          <div className="card p-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">Photos</h2>
            <PhotoUpload onUpload={handleUpload} uploading={uploading} uploadedPhotos={item.photos} />
          </div>
        </div>

        <div className="col-lg-5">
          <div className="card p-3">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <h2 style={{ fontSize: "1rem" }} className="mb-0">Claims</h2>
              <span className="stamp stamp-caution">{claims.length}</span>
            </div>
            {claims.length === 0 ? (
              <p className="text-muted-soft mb-0" style={{ fontSize: "0.85rem" }}>No claims submitted yet.</p>
            ) : (
              <div className="list-group list-group-flush">
                {claims.map((c) => (
                  <Link
                    key={c.claimId}
                    to={`/police/claims/${c.claimId}`}
                    className="list-group-item list-group-item-action px-0 d-flex justify-content-between align-items-center"
                  >
                    <span>{c.claimantName}</span>
                    <StatusStamp status={c.status} />
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
