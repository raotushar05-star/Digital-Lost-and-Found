import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { claimService } from "../../services/claimService";
import { policeService } from "../../services/policeService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDateTime } from "../../utils/format";
import ProtectedFileLink from "../../components/common/ProtectedFileLink.jsx";
import DownloadServerPdfButton from "../../components/common/DownloadServerPdfButton.jsx";
import ProtectedImage from "../../components/common/ProtectedImage.jsx";

export default function PoliceClaimDetailPage() {
  const { id } = useParams();
  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [error, setError] = useState(null);
  const [notes, setNotes] = useState("");
  const [working, setWorking] = useState(false);
  const [handoverForm, setHandoverForm] = useState({ handoverNotes: "", acknowledgementReference: "", otp: "" });
  const [handoverDone, setHandoverDone] = useState(null);

  const load = () => {
    setLoading(true);
    setLoadError(null);
    claimService.getById(id)
      .then(setClaim)
      .catch((err) => setLoadError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, [id]);

  const handleVerify = async (decision) => {
    setWorking(true);
    setError(null);
    try {
      await policeService.verifyClaim(id, { decision, verificationNotes: notes });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setWorking(false);
    }
  };

  const handleDispute = async () => {
    const reason = window.prompt("Reason for raising a dispute on this claim:");
    if (!reason) return;
    setError(null);
    try {
      await policeService.raiseDispute(claim.foundItemId, { claimId: id, reason });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleHandover = async (e) => {
    e.preventDefault();
    setWorking(true);
    setError(null);
    try {
      const res = await policeService.recordHandover(id, {
        recipientId: claim.claimantId,
        ...handoverForm
      });
      setHandoverDone(res);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setWorking(false);
    }
  };

  if (loading) return <AppLayout><LoadingSpinner /></AppLayout>;

  if (loadError) {
    return (
      <AppLayout>
        <div className="empty-state">
          <h5 className="font-display mb-2">Couldn't load this claim</h5>
          <p className="mb-3">{loadError}</p>
          <button className="btn btn-sm btn-outline-primary" onClick={load}>Try again</button>
        </div>
      </AppLayout>
    );
  }

  if (!claim) return <AppLayout><LoadingSpinner /></AppLayout>;

  const decidable = claim.status === "PENDING" || claim.status === "UNDER_VERIFICATION" || claim.status === "DISPUTED";

  return (
    <AppLayout>
      <ErrorAlert message={error} />
      <div className="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-2">
        <div>
          <div className="page-eyebrow mb-1">Claim review</div>
          <div className="d-flex align-items-center gap-2">
            <h1 style={{ fontSize: "1.5rem" }} className="mb-0">{claim.claimantName}</h1>
            <StatusStamp status={claim.status} />
          </div>
        </div>
        <Link to={`/police/found-items/${claim.foundItemId}`} className="btn btn-sm btn-outline-primary">
          View item record
        </Link>
      </div>

      <div className="row g-4">
        <div className="col-lg-7">
          <div className="card p-3 mb-3">
            <div className="page-eyebrow mb-1">Claimant's statement</div>
            <p className="mb-0">{claim.claimDetails}</p>
          </div>
          {claim.lostItemPhotos?.length > 0 && (
            <div className="card p-3 mb-3">
              <h2 style={{ fontSize: "1rem" }} className="mb-1">Photos from the claimant's lost report</h2>
              <p className="text-muted-soft mb-2" style={{ fontSize: "0.8rem" }}>
                Unverified context only. The claimant uploaded these before filing this claim, and they could be copied
                from a public listing. Do not treat them as proof of ownership; use the evidence below for that.
              </p>
              <div className="d-flex flex-wrap gap-2">
                {claim.lostItemPhotos.map((p) => (
                  <ProtectedImage
                    key={p.photoId}
                    fileUrl={p.fileUrl}
                    alt="Lost report"
                    style={{ width: 96, height: 96, objectFit: "cover", borderRadius: "var(--radius-sm)", border: "1px solid var(--line)" }}
                  />
                ))}
              </div>
            </div>
          )}
          <div className="card p-3 mb-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">Evidence submitted</h2>
            {claim.evidence && claim.evidence.length > 0 ? (
              <div className="list-group list-group-flush">
                {claim.evidence.map((ev) => (
                  <div key={ev.evidenceId} className="list-group-item px-0 d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium">{ev.evidenceType.replaceAll("_", " ")}</div>
                      <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>{ev.description}</div>
                      {ev.fileUrl && <ProtectedFileLink fileUrl={ev.fileUrl} />}
                    </div>
                    <StatusStamp status={ev.verificationStatus} />
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-soft mb-0" style={{ fontSize: "0.85rem" }}>No evidence submitted yet.</p>
            )}
          </div>

          {decidable && (
            <div className="card p-3 mb-3">
              <h2 style={{ fontSize: "1rem" }} className="mb-2">Ownership verification decision</h2>
              <textarea
                className="form-control mb-2"
                rows={2}
                placeholder="Verification notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
              />
              <div className="d-flex gap-2">
                <button className="btn btn-primary btn-sm" disabled={working} onClick={() => handleVerify("APPROVED")}>
                  Approve claim
                </button>
                <button className="btn btn-outline-danger btn-sm" disabled={working} onClick={() => handleVerify("REJECTED")}>
                  Reject claim
                </button>
                <button className="btn btn-outline-secondary btn-sm" disabled={working} onClick={handleDispute}>
                  Raise dispute
                </button>
              </div>
            </div>
          )}

          {claim.status === "APPROVED" && !claim.handoverRecorded && !handoverDone && (
            <form onSubmit={handleHandover} className="card p-3">
              <h2 style={{ fontSize: "1rem" }} className="mb-2">Record handover</h2>
              <p className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                Confirms the item was physically returned to {claim.claimantName}.
              </p>
              <div className="mb-2">
                <label className="form-label">Claimant's handover code (6 digits)</label>
                <input
                  className="form-control font-mono"
                  required
                  inputMode="numeric"
                  maxLength={6}
                  autoComplete="off"
                  value={handoverForm.otp}
                  onChange={(e) => setHandoverForm({ ...handoverForm, otp: e.target.value.replace(/\D/g, "") })}
                />
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>
                  The claimant gets this code from their claim page.
                </div>
              </div>
              <div className="mb-2">
                <label className="form-label">Acknowledgement reference (optional)</label>
                <input
                  className="form-control"
                  value={handoverForm.acknowledgementReference}
                  onChange={(e) => setHandoverForm({ ...handoverForm, acknowledgementReference: e.target.value })}
                />
              </div>
              <div className="mb-2">
                <label className="form-label">Notes (optional)</label>
                <textarea
                  className="form-control"
                  rows={2}
                  value={handoverForm.handoverNotes}
                  onChange={(e) => setHandoverForm({ ...handoverForm, handoverNotes: e.target.value })}
                />
              </div>
              <button className="btn btn-primary btn-sm" disabled={working}>
                {working ? "Recording…" : "Confirm handover"}
              </button>
            </form>
          )}

          {(handoverDone || claim.handoverRecorded) && (
            <div className="alert alert-success d-flex justify-content-between align-items-center">
              <span>Handover recorded{handoverDone ? ` at ${formatDateTime(handoverDone.handoverDate)}` : ""}.</span>
              <DownloadServerPdfButton
                label="Download receipt"
                fallbackName="findline-handover-receipt.pdf"
                load={() => claimService.downloadReceipt(id)}
              />
            </div>
          )}
        </div>
      </div>
    </AppLayout>
  );
}
