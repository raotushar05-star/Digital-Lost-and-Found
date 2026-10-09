import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { feedbackService } from "../../services/feedbackService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDate } from "../../utils/format";

const STATUSES = ["OPEN", "IN_REVIEW", "RESOLVED", "CLOSED"];

export default function FeedbackInboxPage() {
  const [submissions, setSubmissions] = useState([]);
  const [drafts, setDrafts] = useState({});
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [savingId, setSavingId] = useState(null);

  const load = () => {
    setLoading(true);
    feedbackService.getAll()
      .then((items) => {
        setSubmissions(items);
        setDrafts(Object.fromEntries(items.map((item) => [item.feedbackId, {
          status: item.status,
          adminResponse: item.adminResponse || ""
        }])));
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const updateDraft = (id, changes) => setDrafts((current) => ({
    ...current,
    [id]: { ...current[id], ...changes }
  }));

  const saveResponse = async (submission) => {
    const draft = drafts[submission.feedbackId];
    setSavingId(submission.feedbackId);
    setError(null);
    try {
      await feedbackService.respond(submission.feedbackId, draft);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSavingId(null);
    }
  };

  const filtered = submissions.filter((submission) =>
    (typeFilter === "ALL" || submission.feedbackType === typeFilter)
    && (statusFilter === "ALL" || submission.status === statusFilter));

  return (
    <AppLayout>
      <PageHeader
        eyebrow="System administration"
        title="Feedback & complaints"
        actions={
          <div className="d-flex gap-2">
            <select aria-label="Filter by type" className="form-select form-select-sm" value={typeFilter} onChange={(event) => setTypeFilter(event.target.value)}>
              <option value="ALL">All types</option>
              <option value="FEEDBACK">Feedback</option>
              <option value="COMPLAINT">Complaints</option>
            </select>
            <select aria-label="Filter by status" className="form-select form-select-sm" value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
              <option value="ALL">All statuses</option>
              {STATUSES.map((status) => <option key={status} value={status}>{status.replace("_", " ")}</option>)}
            </select>
          </div>
        }
      />
      {error && <ErrorAlert message={error} />}
      {loading ? <LoadingSpinner /> : filtered.length === 0 ? (
        <EmptyState title="Inbox is clear" message="New feedback and complaints from users and police staff will appear here." />
      ) : (
        <div className="d-flex flex-column gap-3">
          {filtered.map((submission) => {
            const draft = drafts[submission.feedbackId] || { status: submission.status, adminResponse: "" };
            return (
              <article key={submission.feedbackId} className="card p-3">
                <div className="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-2">
                  <div>
                    <div className="page-eyebrow">{submission.feedbackType} · {formatDate(submission.createdAt)}</div>
                    <h2 className="mb-1" style={{ fontSize: "1.05rem" }}>{submission.subject}</h2>
                    <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                      {submission.userName} · {submission.userEmail || submission.userPhone}
                    </div>
                  </div>
                  <StatusStamp status={submission.status} />
                </div>
                <p style={{ whiteSpace: "pre-wrap" }}>{submission.message}</p>
                <div className="row g-2 align-items-end">
                  <div className="col-sm-3">
                    <label htmlFor={`feedback-status-${submission.feedbackId}`} className="form-label">Status</label>
                    <select id={`feedback-status-${submission.feedbackId}`} className="form-select" value={draft.status} onChange={(event) => updateDraft(submission.feedbackId, { status: event.target.value })}>
                      {STATUSES.map((status) => <option key={status} value={status}>{status.replace("_", " ")}</option>)}
                    </select>
                  </div>
                  <div className="col-sm-7">
                    <label htmlFor={`feedback-response-${submission.feedbackId}`} className="form-label">Response to submitter</label>
                    <textarea id={`feedback-response-${submission.feedbackId}`} className="form-control" rows={2} value={draft.adminResponse} onChange={(event) => updateDraft(submission.feedbackId, { adminResponse: event.target.value })} />
                  </div>
                  <div className="col-sm-2">
                    <button className="btn btn-primary w-100" disabled={savingId === submission.feedbackId || !draft.adminResponse.trim()} onClick={() => saveResponse(submission)}>
                      {savingId === submission.feedbackId ? "Saving…" : "Save reply"}
                    </button>
                  </div>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </AppLayout>
  );
}