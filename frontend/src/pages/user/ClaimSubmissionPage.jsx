import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import { searchService } from "../../services/searchService";
import { claimService } from "../../services/claimService";
import { userService } from "../../services/userService";
import { extractErrorMessage } from "../../services/apiClient";

export default function ClaimSubmissionPage() {
  const { foundItemId } = useParams();
  const navigate = useNavigate();
  const [item, setItem] = useState(null);
  const [lostItems, setLostItems] = useState([]);
  const [claimDetails, setClaimDetails] = useState("");
  const [lostItemId, setLostItemId] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    Promise.all([searchService.getPublicDetail(foundItemId), userService.getMyLostItems()])
      .then(([itemData, myLostItems]) => {
        setItem(itemData);
        setLostItems(myLostItems.filter((li) => li.status !== "RESOLVED" && li.status !== "WITHDRAWN"));
      })
      .finally(() => setLoading(false));
  }, [foundItemId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const res = await claimService.create(foundItemId, {
        lostItemId: lostItemId || null,
        claimDetails
      });
      navigate(`/claims/${res.claimId}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <AppLayout><LoadingSpinner /></AppLayout>;

  return (
    <AppLayout>
      <PageHeader eyebrow="Ownership claim" title={`Claim: ${item?.category}`} />
      <div className="row g-4">
        <div className="col-lg-5">
          <div className="card p-3">
            <div className="page-eyebrow mb-2">Item summary</div>
            <p className="mb-1">{item?.description}</p>
            <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
              {item?.color} {item?.brand} · {item?.location?.city}
            </div>
          </div>
        </div>
        <div className="col-lg-7">
          <form onSubmit={handleSubmit} className="card p-4">
            <ErrorAlert message={error} />

            {lostItems.length > 0 && (
              <div className="mb-3">
                <label className="form-label">Link to one of your lost-item reports (optional)</label>
                <select className="form-select" value={lostItemId} onChange={(e) => setLostItemId(e.target.value)}>
                  <option value="">— None —</option>
                  {lostItems.map((li) => (
                    <option key={li.lostItemId} value={li.lostItemId}>
                      {li.category} — {li.description?.slice(0, 40)}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="mb-3">
              <label className="form-label">Why is this yours?</label>
              <textarea
                className="form-control"
                rows={5}
                required
                placeholder="Describe unique details only the owner would know: serial numbers, contents, marks, purchase details…"
                value={claimDetails}
                onChange={(e) => setClaimDetails(e.target.value)}
              />
            </div>

            <p className="text-muted-soft" style={{ fontSize: "0.8rem" }}>
              After submitting, you'll be able to attach supporting evidence (receipts, photos, serial numbers)
              for police to review.
            </p>

            <button className="btn btn-primary" disabled={submitting}>
              {submitting ? "Submitting…" : "Submit claim"}
            </button>
          </form>
        </div>
      </div>
    </AppLayout>
  );
}
