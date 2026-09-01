import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { searchService } from "../../services/searchService";
import { useAuth } from "../../context/AuthContext.jsx";
import { formatDate } from "../../utils/format";

export default function FoundItemPublicDetailPage() {
  const { id } = useParams();
  const { isAuthenticated } = useAuth();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    searchService
      .getPublicDetail(id)
      .then(setItem)
      .catch(() => setError("This item is not available for public viewing."))
      .finally(() => setLoading(false));
  }, [id]);

  return (
    <AppLayout>
      {loading ? (
        <LoadingSpinner />
      ) : error ? (
        <div className="alert alert-warning">{error}</div>
      ) : (
        <div className="row g-4">
          <div className="col-lg-7">
            <div className="page-eyebrow mb-1">Found item</div>
            <div className="d-flex align-items-center gap-2 mb-2">
              <h1 style={{ fontSize: "1.6rem" }} className="mb-0">
                {item.category}
              </h1>
              <StatusStamp status={item.verificationStatus} />
            </div>
            {item.photos && item.photos.length > 0 ? (
              <div className="d-flex gap-2 flex-wrap mb-3">
                {item.photos.map((p) => (
                  <img
                    key={p.photoId}
                    src={p.fileUrl}
                    alt={item.category}
                    style={{ width: 180, height: 180, objectFit: "cover", borderRadius: "var(--radius)", border: "1px solid var(--line)" }}
                  />
                ))}
              </div>
            ) : (
              <div className="empty-state mb-3">No photos have been uploaded for this item yet.</div>
            )}
            <div className="card p-3 mb-3">
              <p className="mb-2">{item.description}</p>
              <div className="row">
                <div className="col-6 col-md-3">
                  <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Color</div>
                  <div>{item.color || "—"}</div>
                </div>
                <div className="col-6 col-md-3">
                  <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Brand</div>
                  <div>{item.brand || "—"}</div>
                </div>
                <div className="col-6 col-md-3">
                  <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Found date</div>
                  <div>{formatDate(item.foundDate)}</div>
                </div>
                <div className="col-6 col-md-3">
                  <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Area</div>
                  <div>{item.location?.city}</div>
                </div>
              </div>
            </div>

            {isAuthenticated ? (
              <Link to={`/found-items/${id}/claim`} className="btn btn-primary">
                This is mine — submit a claim
              </Link>
            ) : (
              <div className="alert alert-info">
                <Link to="/login" state={{ from: { pathname: `/found-items/${id}/claim` } }}>
                  Sign in
                </Link>{" "}
                to submit an ownership claim for this item.
              </div>
            )}
          </div>
          <div className="col-lg-5">
            <div className="card p-3">
              <div className="page-eyebrow mb-2">Approximate location</div>
              <p className="text-muted-soft mb-0" style={{ fontSize: "0.85rem" }}>
                {item.location?.addressText || item.location?.city}, {item.location?.state}
              </p>
            </div>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
