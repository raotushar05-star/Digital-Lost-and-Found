import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { adminService } from "../../services/adminService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDateTime } from "../../utils/format";

export default function AuditLogPage() {
  const [action, setAction] = useState("");
  const [q, setQ] = useState("");
  const [applied, setApplied] = useState({ action: "", q: "" });
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = (targetPage, filters) => {
    setLoading(true);
    setError(null);
    adminService
      .getAuditLogs({ page: targetPage, size: 25, action: filters.action, q: filters.q })
      .then((res) => {
        setData(res);
        setPage(targetPage);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load(0, applied);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    const filters = { action: action.trim(), q: q.trim() };
    setApplied(filters);
    load(0, filters);
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="System administration"
        title="Audit log"
        subtitle="Who did what, and when. Newest first."
        actions={
          <form className="d-flex flex-wrap gap-2 align-items-end" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="audit-action" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Action</label>
              <input id="audit-action" className="form-control form-control-sm" placeholder="e.g. LOGIN" value={action} onChange={(e) => setAction(e.target.value)} />
            </div>
            <div>
              <label htmlFor="audit-user" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>User</label>
              <input id="audit-user" className="form-control form-control-sm" placeholder="Name or email" value={q} onChange={(e) => setQ(e.target.value)} />
            </div>
            <button className="btn btn-sm btn-primary">Search</button>
          </form>
        }
      />
      {error && <ErrorAlert message={error} />}
      {loading ? (
        <LoadingSpinner />
      ) : !data || data.content.length === 0 ? (
        <EmptyState title="No entries" message="Nothing matches these filters." />
      ) : (
        <>
          <div className="card">
            <div className="table-responsive">
              <table className="table table-sm align-middle mb-0">
                <thead>
                  <tr className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                    <th className="ps-3">When</th>
                    <th>User</th>
                    <th>Action</th>
                    <th>Entity</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((a) => (
                    <tr key={a.auditId} style={{ fontSize: "0.85rem" }}>
                      <td className="ps-3 font-mono">{formatDateTime(a.createdAt)}</td>
                      <td>
                        {a.userName || "System"}
                        {a.userRole && <span className="text-muted-soft"> · {a.userRole.replaceAll("_", " ")}</span>}
                      </td>
                      <td className="font-mono">{a.action}</td>
                      <td className="text-muted-soft">{a.entityType}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="d-flex justify-content-center gap-2 mt-3">
            <button className="btn btn-sm btn-outline-primary" disabled={page === 0} onClick={() => load(page - 1, applied)}>Previous</button>
            <span className="text-muted-soft align-self-center font-mono" style={{ fontSize: "0.8rem" }}>
              Page {page + 1} of {Math.max(data.totalPages, 1)}
            </span>
            <button className="btn btn-sm btn-outline-primary" disabled={page + 1 >= data.totalPages} onClick={() => load(page + 1, applied)}>Next</button>
          </div>
        </>
      )}
    </AppLayout>
  );
}