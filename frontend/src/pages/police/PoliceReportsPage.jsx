import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import DownloadServerPdfButton from "../../components/common/DownloadServerPdfButton.jsx";
import { policeService } from "../../services/policeService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDate, formatDateTime, timeAgo } from "../../utils/format";
import { getStatusMeta } from "../../utils/statusConfig";

const dash = (value) => (value === null || value === undefined || value === "" ? "—" : value);

function Field({ label, value, wide = false }) {
  return (
    <div className={wide ? "col-12" : "col-sm-6 col-lg-4"}>
      <div className="text-muted-soft" style={{ fontSize: "0.72rem" }}>{label}</div>
      <div style={{ fontSize: "0.9rem", overflowWrap: "anywhere" }}>{dash(value)}</div>
    </div>
  );
}

function Section({ title, children }) {
  return (
    <section className="mb-4">
      <div className="page-eyebrow mb-2">{title}</div>
      {children}
    </section>
  );
}

function NoneLine({ children }) {
  return <div className="text-muted-soft" style={{ fontSize: "0.85rem" }}>{children}</div>;
}

function caseTitle(c) {
  const category = c.property?.category || (c.caseType === "LOST" ? "Lost item" : "Found item");
  const description = c.property?.description;
  if (!description) return category;
  return `${category} · ${description.length > 60 ? `${description.slice(0, 60)}…` : description}`;
}

function CaseDetails({ c }) {
  const station = c.station || {};
  const person = c.complainant || {};
  const occ = c.occurrence || {};
  const prop = c.property || {};
  const custody = c.custody;
  const finder = custody?.finder;
  const matches = c.matches || [];
  const claims = c.claims || [];
  const disputes = c.disputes || [];
  const diary = c.caseDiary || [];
  const handover = c.handover;
  const photos = prop.photoUrls || [];
  const place = [occ.addressText, occ.locality, occ.city, occ.state, occ.postalCode].filter(Boolean).join(", ");

  return (
    <div>
      <Section title="Registration & station">
        <div className="row g-3">
          <Field label="Case number" value={c.caseNumber} />
          <Field label="Case type" value={c.caseType === "LOST" ? "Lost property" : "Found property"} />
          <Field label="Current status" value={getStatusMeta(c.currentStatus).label} />
          <Field label="Registered" value={formatDateTime(c.registeredAt)} />
          <Field label="Last updated" value={formatDateTime(c.lastUpdatedAt)} />
          <Field label="Investigating station" value={station.stationName} />
          <Field label="Station phone" value={station.phone} />
        </div>
      </Section>

      <Section title={person.roleLabel || "Complainant / informant"}>
        <div className="row g-3">
          <Field label="Name" value={person.name} />
          <Field label="Phone" value={person.phone} />
          <Field label="Email" value={person.email} />
          <Field label="Address" value={person.address} wide />
        </div>
      </Section>

      <Section title="Place and time of occurrence">
        <div className="row g-3">
          <Field label="Date" value={formatDate(occ.occurrenceDate)} />
          <Field
            label="Coordinates"
            value={occ.latitude != null ? `${occ.latitude}, ${occ.longitude}` : null}
          />
          <Field label="Place" value={place} wide />
        </div>
      </Section>

      <Section title="Property">
        <div className="row g-3">
          <Field label="Category" value={prop.category} />
          <Field label="Brand" value={prop.brand} />
          <Field label="Colour" value={prop.color} />
          <Field label="Item status" value={prop.itemStatus ? getStatusMeta(prop.itemStatus).label : null} />
          <Field label="Description" value={prop.description} wide />
          <Field label="Identifying marks / serial (restricted)" value={prop.identifyingDetails} wide />
        </div>
        {photos.length > 0 && (
          <div className="d-flex flex-wrap gap-2 mt-3">
            {photos.map((url, index) => (
              <img
                key={`${url}-${index}`}
                src={url}
                alt="Case item"
                style={{ width: 96, height: 96, objectFit: "cover", borderRadius: "var(--radius-sm)", border: "1px solid var(--line)" }}
              />
            ))}
          </div>
        )}
      </Section>

      {custody && (
        <Section title="Custody">
          <div className="row g-3">
            <Field label="Custody status" value={custody.custodyStatus ? getStatusMeta(custody.custodyStatus).label : null} />
            <Field label="Verification" value={custody.verificationStatus ? getStatusMeta(custody.verificationStatus).label : null} />
            <Field label="Received at station" value={formatDateTime(custody.receivedAt)} />
            <Field label="Finder" value={finder?.name} />
            <Field label="Finder phone" value={finder?.phone} />
            <Field label="Finder email" value={finder?.email} />
          </div>
        </Section>
      )}

      <Section title={`Matches (${matches.length})`}>
        {matches.length === 0 ? (
          <NoneLine>No system matches recorded.</NoneLine>
        ) : (
          <div className="list-group list-group-flush">
            {matches.map((m) => (
              <div key={m.matchId} className="list-group-item px-0">
                <div className="d-flex justify-content-between gap-2">
                  <span className="fw-medium">{dash(m.counterpartType)} · {dash(m.counterpartCategory)}</span>
                  <span className="font-mono text-brass" style={{ fontSize: "0.78rem" }}>
                    {m.matchScore != null ? `${Number(m.matchScore).toFixed(0)}%` : "—"}
                  </span>
                </div>
                <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>{m.counterpartDescription}</div>
                <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>{m.matchReason}</div>
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title={`Claims (${claims.length})`}>
        {claims.length === 0 ? (
          <NoneLine>No claims filed.</NoneLine>
        ) : (
          <div className="list-group list-group-flush">
            {claims.map((cl) => (
              <div key={cl.claimId} className="list-group-item px-0">
                <div className="d-flex justify-content-between align-items-center gap-2">
                  <span className="fw-medium">{dash(cl.claimantName)}</span>
                  <StatusStamp status={cl.status} />
                </div>
                <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>
                  {[cl.claimantPhone, cl.claimantEmail].filter(Boolean).join(" · ")}
                </div>
                <div style={{ fontSize: "0.88rem" }}>{cl.claimDetails}</div>
                {(cl.evidence || []).length > 0 && (
                  <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                    Evidence: {cl.evidence.map((e) => (e.evidenceType || e.description || "").replaceAll("_", " ")).join("; ")}
                  </div>
                )}
                {cl.reviewedByName && (
                  <div className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                    Reviewed by {cl.reviewedByName} on {formatDateTime(cl.reviewedAt)}
                  </div>
                )}
                <Link to={`/police/claims/${cl.claimId}`} style={{ fontSize: "0.82rem" }}>Open claim →</Link>
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title={`Disputes (${disputes.length})`}>
        {disputes.length === 0 ? (
          <NoneLine>No disputes.</NoneLine>
        ) : (
          <div className="list-group list-group-flush">
            {disputes.map((d) => (
              <div key={d.disputeId} className="list-group-item px-0">
                <div className="d-flex justify-content-between align-items-center gap-2">
                  <span className="fw-medium">Raised by {dash(d.raisedByName)}</span>
                  <StatusStamp status={d.status} />
                </div>
                <div style={{ fontSize: "0.88rem" }}>{d.reason}</div>
                {d.resolution && (
                  <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>
                    Resolution ({formatDateTime(d.resolvedAt)}, {dash(d.resolvedByName)}): {d.resolution}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title="Handover">
        {!handover ? (
          <NoneLine>Property not yet handed over.</NoneLine>
        ) : (
          <div className="row g-3">
            <Field label="Handover date" value={formatDateTime(handover.handoverDate)} />
            <Field label="Recipient" value={handover.recipientName} />
            <Field label="Recipient phone" value={handover.recipientPhone} />
            <Field label="Duty officer" value={handover.officerName} />
            <Field label="Receipt / reference" value={handover.acknowledgementReference} />
            <Field label="Notes" value={handover.notes} wide />
          </div>
        )}
      </Section>

      <Section title="Case diary">
        {diary.length === 0 ? (
          <NoneLine>No case diary entries.</NoneLine>
        ) : (
          <div className="ledger-timeline">
            {diary.map((h, index) => (
              <div className="ledger-step is-done" key={index}>
                <div className="ledger-dot" />
                <div>
                  <div className="ledger-label">{getStatusMeta(h.newStatus).label}</div>
                  {h.remarks && (
                    <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>{h.remarks}</div>
                  )}
                  <div className="ledger-meta">
                    {formatDateTime(h.changedAt)}{h.recordedByName ? ` · ${h.recordedByName}` : ""}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Section>
    </div>
  );
}

export default function PoliceReportsPage() {
  const [cases, setCases] = useState([]);
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [appliedRange, setAppliedRange] = useState({ dateFrom: "", dateTo: "" });
  const [query, setQuery] = useState("");
  const [openId, setOpenId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = () => {
    setLoading(true);
    setError(null);
    const params = {};
    if (dateFrom) params.dateFrom = dateFrom;
    if (dateTo) params.dateTo = dateTo;
    policeService.getCaseFiles(params)
      .then((data) => {
        setCases(data);
        setAppliedRange({ dateFrom, dateTo });
        setOpenId(null);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const visible = useMemo(() => {
    const q = query.trim().toLowerCase();
    return [...cases]
      .filter((c) =>
        !q ||
        [c.caseNumber, c.property?.category, c.property?.description, c.currentStatus, c.complainant?.name]
          .filter(Boolean)
          .join(" ")
          .toLowerCase()
          .includes(q))
      .sort((a, b) => new Date(b.lastUpdatedAt || 0) - new Date(a.lastUpdatedAt || 0));
  }, [cases, query]);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Case records"
        title="Reports"
        subtitle="Every case on record. Open a case for its full details, or download it as a PDF."
        actions={
          <form
            className="d-flex flex-wrap gap-2 align-items-end"
            onSubmit={(e) => {
              e.preventDefault();
              load();
            }}
          >
            <div>
              <label htmlFor="reports-search" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Search</label>
              <input
                id="reports-search"
                className="form-control form-control-sm"
                placeholder="Case no., item, status…"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="reports-date-from" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>From</label>
              <input id="reports-date-from" type="date" className="form-control form-control-sm" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
            </div>
            <div>
              <label htmlFor="reports-date-to" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>To</label>
              <input id="reports-date-to" type="date" className="form-control form-control-sm" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
            </div>
            <button className="btn btn-sm btn-outline-primary">Apply</button>
            <DownloadServerPdfButton
              label="All cases (PDF)"
              fallbackName="findline-all-cases.pdf"
              className="btn btn-sm btn-primary"
              load={() =>
                policeService.downloadAllCaseFilesPdf({
                  ...(appliedRange.dateFrom && { dateFrom: appliedRange.dateFrom }),
                  ...(appliedRange.dateTo && { dateTo: appliedRange.dateTo })
                })
              }
            />
          </form>
        }
      />

      {error && <ErrorAlert message={error} />}

      {loading ? (
        <LoadingSpinner />
      ) : visible.length === 0 ? (
        <EmptyState
          title={cases.length === 0 ? "No cases yet" : "No matching cases"}
          message={cases.length === 0 ? "Cases appear here as soon as reports are filed." : "Try a different search or date range."}
        />
      ) : (
        <div>
          <div className="text-muted-soft font-mono mb-2" style={{ fontSize: "0.75rem" }}>
            {visible.length} {visible.length === 1 ? "case" : "cases"} · most recently updated first
          </div>
          {visible.map((c) => {
            const isOpen = openId === c.caseId;
            return (
              <div className="card mb-3" key={c.caseId}>
                <div className="d-flex align-items-center">
                  <button
                    type="button"
                    className="flex-grow-1 text-start bg-transparent border-0 p-3"
                    style={{ minWidth: 0, color: "inherit" }}
                    aria-expanded={isOpen}
                    onClick={() => setOpenId(isOpen ? null : c.caseId)}
                  >
                    <div className="d-flex flex-wrap align-items-center gap-2">
                      <span className="text-muted-soft" aria-hidden="true">{isOpen ? "▾" : "▸"}</span>
                      <span className="fw-medium">{caseTitle(c)}</span>
                      <span className="stamp stamp-neutral">{c.caseType === "LOST" ? "Lost" : "Found"}</span>
                      <StatusStamp status={c.currentStatus} />
                    </div>
                    <div className="text-muted-soft mt-1" style={{ fontSize: "0.8rem" }}>
                      <span className="font-mono">{c.caseNumber}</span>
                      {c.station?.stationName ? ` · ${c.station.stationName}` : ""}
                      {" · "}Last updated {formatDateTime(c.lastUpdatedAt)}
                      {c.lastUpdatedAt ? ` (${timeAgo(c.lastUpdatedAt)})` : ""}
                    </div>
                  </button>
                  <div className="pe-3 ps-2 flex-shrink-0">
                    <DownloadServerPdfButton
                      label="Download"
                      fallbackName={`findline-case-${c.caseNumber || "case"}.pdf`}
                      load={() => policeService.downloadCaseFilePdf(c.caseId)}
                    />
                  </div>
                </div>
                {isOpen && (
                  <div className="hairline-top p-3">
                    <CaseDetails c={c} />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </AppLayout>
  );
}