import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { policeService } from "../../services/policeService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDate } from "../../utils/format";

const STATUSES = [
  ["REPORTED", "Reported"],
  ["RECEIVED", "Received by police"],
  ["POTENTIAL_MATCH", "Potential match"],
  ["CLAIM_SUBMITTED", "Claim submitted"],
  ["UNDER_VERIFICATION", "Under verification"],
  ["APPROVED", "Approved"],
  ["REJECTED", "Rejected"],
  ["RETURNED", "Returned"],
  ["RESOLVED", "Resolved"],
  ["WITHDRAWN", "Withdrawn"]
];

export default function PoliceLostReportsPage() {
  const [reports, setReports] = useState([]);
  const [status, setStatus] = useState("ALL");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    policeService.getLostReports()
      .then(setReports)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const normalizedQuery = query.trim().toLocaleLowerCase();
  const filteredReports = reports.filter((report) => {
    const matchesStatus = status === "ALL" || report.status === status;
    const searchable = [report.caseNumber, report.category, report.description, report.city]
      .filter(Boolean)
      .join(" ")
      .toLocaleLowerCase();
    return matchesStatus && searchable.includes(normalizedQuery);
  });

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Citizen reports"
        title="Lost-item reports"
        subtitle="Digital reports submitted by citizens, available to authorized police staff."
        actions={
          <div className="d-flex flex-wrap align-items-end gap-2">
            <div>
              <label htmlFor="lost-report-search" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Search reports</label>
              <input
                id="lost-report-search"
                className="form-control form-control-sm"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Case, item, or city"
              />
            </div>
            <div>
              <label htmlFor="lost-report-status" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Status</label>
              <select id="lost-report-status" className="form-select form-select-sm" value={status} onChange={(event) => setStatus(event.target.value)}>
                <option value="ALL">All statuses</option>
                {STATUSES.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
            </div>
          </div>
        }
      />
      {error && <ErrorAlert message={error} />}
      {loading ? <LoadingSpinner /> : filteredReports.length === 0 ? (
        <EmptyState
          title={reports.length === 0 ? "No lost-item reports yet" : "No matching reports"}
          message={reports.length === 0 ? "Citizen-submitted reports will appear here." : "Try changing the search text or status filter."}
        />
      ) : (
        <div className="card">
          <div className="list-group list-group-flush">
            {filteredReports.map((report) => (
              <Link
                key={report.lostItemId}
                to={`/police/lost-reports/${report.lostItemId}`}
                className="list-group-item list-group-item-action d-flex justify-content-between align-items-center gap-3 p-3"
              >
                <div className="min-w-0">
                  <div className="d-flex flex-wrap align-items-center gap-2">
                    <span className="fw-medium">{report.category}</span>
                    {report.caseNumber && <span className="font-mono text-muted-soft" style={{ fontSize: "0.75rem" }}>{report.caseNumber}</span>}
                  </div>
                  <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>
                    {report.description?.slice(0, 90)} · Lost {formatDate(report.lostDate)} · {report.city} · {report.assignedStationName || "Unassigned"}
                  </div>
                </div>
                <StatusStamp status={report.status} />
              </Link>
            ))}
          </div>
        </div>
      )}
    </AppLayout>
  );
}