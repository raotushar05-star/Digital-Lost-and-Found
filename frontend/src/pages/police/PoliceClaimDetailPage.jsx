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

export default function PoliceClaimDetailPage() {
  const { id } = useParams();
  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notes, setNotes] = useState("");
  const [working, setWorking] = useState(false);
  const [handoverForm, setHandoverForm] = useState({ handoverNotes: "", acknowledgementReference: "" });
  const [handoverDone, setHandoverDone] = useState(null);

  const load = () => {
    setLoading(true);
    claimService.getById(id).then(setClaim).finally(() => setLoading(false));
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

  if (loading || !claim) return <AppLayout><LoadingSpinner /></AppLayout>;

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

          <div className="card p-3 mb-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">Evidence submitted</h2>
            {claim.evidence && claim.evidence.length > 0 ? (
              <div className="list-group list-group-flush">
                {claim.evidence.map((ev) => (
                  <div key={ev.evidenceId} className="list-group-item px-0 d-flex justify-content-between align-items-center">
                    <div>
                      <div className="fw-medium">{ev.evidenceType.replaceAll("_", " ")}</div>
                      <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>{ev.description}</div>
                      {ev.fileUrl && (
                        <a href={ev.fileUrl} target="_blank" rel="noreferrer" style={{ fontSize: "0.8rem" }}>
                          View file
                        </a>
                      )}
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

          {claim.status === "APPROVED" && !handoverDone && (
            <form onSubmit={handleHandover} className="card p-3">
              <h2 style={{ fontSize: "1rem" }} className="mb-2">Record handover</h2>
              <p className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                Confirms the item was physically returned to {claim.claimantName}.
              </p>
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

          {handoverDone && (
            <div className="alert alert-success">
              Handover recorded at {formatDateTime(handoverDone.handoverDate)}.
            </div>
          )}
        </div>
      </div>
    </AppLayout>
  );
}
