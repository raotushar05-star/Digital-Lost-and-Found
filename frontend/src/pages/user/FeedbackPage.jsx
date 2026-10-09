import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { feedbackService } from "../../services/feedbackService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDate } from "../../utils/format";

const emptyForm = { feedbackType: "FEEDBACK", subject: "", message: "" };

export default function FeedbackPage() {
  const [submissions, setSubmissions] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const load = () => {
    setLoading(true);
    feedbackService.getMine()
      .then(setSubmissions)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      await feedbackService.create(form);
      setForm(emptyForm);
      setSuccess("Your message was sent to the system administrator.");
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader eyebrow="Support" title="Feedback & complaints" subtitle="Send a private message to the system administrator." />
      <div className="row g-4">
        <div className="col-lg-5">
          <form className="card p-3" onSubmit={handleSubmit}>
            <h2 className="mb-3" style={{ fontSize: "1rem" }}>New message</h2>
            {error && <ErrorAlert message={error} />}
            {success && <div className="alert alert-success py-2" role="status">{success}</div>}
            <div className="mb-3">
              <label htmlFor="feedback-type" className="form-label">Type</label>
              <select id="feedback-type" className="form-select" value={form.feedbackType} onChange={(event) => setForm({ ...form, feedbackType: event.target.value })}>
                <option value="FEEDBACK">Feedback</option>
                <option value="COMPLAINT">Complaint</option>
              </select>
            </div>
            <div className="mb-3">
              <label htmlFor="feedback-subject" className="form-label">Subject</label>
              <input id="feedback-subject" className="form-control" maxLength={150} required value={form.subject} onChange={(event) => setForm({ ...form, subject: event.target.value })} />
            </div>
            <div className="mb-3">
              <label htmlFor="feedback-message" className="form-label">Message</label>
              <textarea id="feedback-message" className="form-control" rows={6} maxLength={10000} required value={form.message} onChange={(event) => setForm({ ...form, message: event.target.value })} />
            </div>
            <button className="btn btn-primary" disabled={submitting}>{submitting ? "Sending…" : "Send to system admin"}</button>
          </form>
        </div>
        <div className="col-lg-7">
          <h2 className="mb-3" style={{ fontSize: "1rem" }}>Your messages</h2>
          {loading ? <LoadingSpinner /> : submissions.length === 0 ? (
            <EmptyState title="No messages yet" message="Your feedback and complaints will appear here, along with any admin response." />
          ) : (
            <div className="d-flex flex-column gap-3">
              {submissions.map((submission) => (
                <article key={submission.feedbackId} className="card p-3">
                  <div className="d-flex justify-content-between align-items-start gap-2 mb-2">
                    <div>
                      <div className="page-eyebrow">{submission.feedbackType === "COMPLAINT" ? "Complaint" : "Feedback"} · {formatDate(submission.createdAt)}</div>
                      <h3 className="mb-0" style={{ fontSize: "1rem" }}>{submission.subject}</h3>
                    </div>
                    <StatusStamp status={submission.status} />
                  </div>
                  <p className="mb-2" style={{ whiteSpace: "pre-wrap" }}>{submission.message}</p>
                  {submission.adminResponse && (
                    <div className="border-top pt-2 mt-1">
                      <div className="text-muted-soft mb-1" style={{ fontSize: "0.78rem" }}>System admin response</div>
                      <div style={{ whiteSpace: "pre-wrap" }}>{submission.adminResponse}</div>
                    </div>
                  )}
                </article>
              ))}
            </div>
          )}
        </div>
      </div>
    </AppLayout>
  );
}