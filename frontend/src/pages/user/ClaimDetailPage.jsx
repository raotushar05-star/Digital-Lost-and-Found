import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { claimService } from "../../services/claimService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDateTime } from "../../utils/format";

const EVIDENCE_TYPES = ["RECEIPT", "PHOTOGRAPH", "SERIAL_NUMBER", "UNIQUE_MARK", "OTHER"];

export default function ClaimDetailPage() {
  const { id } = useParams();
  const [claim, setClaim] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({ evidenceType: "RECEIPT", description: "", file: null });

  const load = () => {
    setLoading(true);
    claimService.getById(id).then(setClaim).finally(() => setLoading(false));
  };

  useEffect(load, [id]);

  const handleAddEvidence = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await claimService.addEvidence(id, form.evidenceType, form.description, form.file);
      setForm({ evidenceType: "RECEIPT", description: "", file: null });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading || !claim) return <AppLayout><LoadingSpinner /></AppLayout>;

  const finalized = claim.status === "APPROVED" || claim.status === "REJECTED";

  return (
    <AppLayout>
      <div className="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-2">
        <div>
          <div className="page-eyebrow mb-1">Claim</div>
          <div className="d-flex align-items-center gap-2">
            <h1 style={{ fontSize: "1.5rem" }} className="mb-0">{claim.foundItemCategory}</h1>
            <StatusStamp status={claim.status} />
          </div>
        </div>
        <Link to={`/found-items/${claim.foundItemId}`} className="btn btn-sm btn-outline-primary">
          View item
        </Link>
      </div>

      <div className="row g-4">
        <div className="col-lg-7">
          <div className="card p-3 mb-3">
            <div className="page-eyebrow mb-1">Your statement</div>
            <p className="mb-0">{claim.claimDetails}</p>
          </div>

          {claim.reviewedByName && (
            <div className={`alert ${claim.status === "APPROVED" ? "alert-success" : "alert-secondary"}`}>
              Reviewed by {claim.reviewedByName} on {formatDateTime(claim.reviewedAt)}.
              {claim.status === "APPROVED" && " Please visit the station to complete the handover."}
            </div>
          )}

          <div className="card p-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">Evidence</h2>
            {claim.evidence && claim.evidence.length > 0 ? (
              <div className="list-group list-group-flush mb-3">
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
              <p className="text-muted-soft" style={{ fontSize: "0.85rem" }}>No evidence submitted yet.</p>
            )}

            {!finalized && (
              <form onSubmit={handleAddEvidence} className="hairline-top pt-3">
                <ErrorAlert message={error} />
                <div className="row g-2">
                  <div className="col-md-4">
                    <select
                      className="form-select form-select-sm"
                      value={form.evidenceType}
                      onChange={(e) => setForm({ ...form, evidenceType: e.target.value })}
                    >
                      {EVIDENCE_TYPES.map((t) => (
                        <option key={t} value={t}>{t.replaceAll("_", " ")}</option>
                      ))}
                    </select>
                  </div>
                  <div className="col-md-5">
                    <input
                      className="form-control form-control-sm"
                      placeholder="Description"
                      value={form.description}
                      onChange={(e) => setForm({ ...form, description: e.target.value })}
                    />
                  </div>
                  <div className="col-md-3">
                    <input
                      type="file"
                      className="form-control form-control-sm"
                      onChange={(e) => setForm({ ...form, file: e.target.files?.[0] || null })}
                    />
                  </div>
                </div>
                <button className="btn btn-sm btn-outline-primary mt-2" disabled={submitting}>
                  {submitting ? "Adding…" : "Add evidence"}
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  );
}
