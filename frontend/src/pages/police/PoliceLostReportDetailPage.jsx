import DownloadServerPdfButton from "../../components/common/DownloadServerPdfButton.jsx";
import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { policeService } from "../../services/policeService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDate } from "../../utils/format";

export default function PoliceLostReportDetailPage() {
  const { id } = useParams();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [acknowledging, setAcknowledging] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    policeService.getLostReport(id)
      .then(setReport)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id]);

  const acknowledgeReceipt = async () => {
    setAcknowledging(true);
    setError(null);
    try {
      setReport(await policeService.acknowledgeLostReport(id));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setAcknowledging(false);
    }
  };

  if (loading) return <AppLayout><LoadingSpinner /></AppLayout>;

  if (error || !report) {
    return (
      <AppLayout>
        <PageHeader eyebrow="Police · Lost reports" title="Report unavailable" />
        <ErrorAlert message={error || "This report could not be loaded."} />
        <Link to="/police/lost-reports" className="btn btn-sm btn-outline-primary mt-3">Back to lost reports</Link>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Lost report"
        title={report.category}
        subtitle={report.caseNumber ? `Case ${report.caseNumber}` : "Digital citizen report"}
        actions={
          <div className="d-flex gap-2">
            {report.caseId && (
              <DownloadServerPdfButton
                label="Download case PDF"
                fallbackName={`findline-case-${report.caseNumber || "case"}.pdf`}
                load={() => policeService.downloadCaseFilePdf(report.caseId)}
              />
            )}
            <Link to="/police/lost-reports" className="btn btn-sm btn-outline-primary">Back to reports</Link>
            {report.status === "REPORTED" && (
              <button className="btn btn-sm btn-primary" onClick={acknowledgeReceipt} disabled={acknowledging}>
                {acknowledging ? "Recording…" : "Mark received"}
              </button>
            )}
          </div>
        }
      />
      {error && <ErrorAlert message={error} />}
      <div className="row g-3">
        <div className="col-lg-8">
          <section className="card p-3 mb-3">
            <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
              <h2 className="mb-0" style={{ fontSize: "1rem" }}>Reported item</h2>
              <StatusStamp status={report.status} />
            </div>
            <p>{report.description}</p>
            <div className="row g-3">
              <div className="col-sm-4"><div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Lost date</div>{formatDate(report.lostDate)}</div>
              <div className="col-sm-4"><div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Location</div>{[report.location?.locality, report.location?.city].filter(Boolean).join(", ") || "Not provided"}</div>
              <div className="col-sm-4"><div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Assigned station</div>{report.assignedStationName || "Unassigned"}</div>
              <div className="col-sm-4"><div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Brand / color</div>{[report.brand, report.color].filter(Boolean).join(" · ") || "Not provided"}</div>
            </div>
            {report.identifyingDetails && (
              <div className="mt-3">
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Private identifying details</div>
                <div>{report.identifyingDetails}</div>
              </div>
            )}
          </section>
          <section className="card p-3">
            <h2 className="mb-3" style={{ fontSize: "1rem" }}>Submitted photos</h2>
            {report.photos?.length ? (
              <div className="d-flex flex-wrap gap-2">
                {report.photos.map((photo) => (
                  <img key={photo.photoId} src={photo.fileUrl} alt="Lost item report" style={{ width: 150, height: 150, objectFit: "cover", borderRadius: "var(--radius-sm)", border: "1px solid var(--line)" }} />
                ))}
              </div>
            ) : <p className="text-muted-soft mb-0">No photos were submitted.</p>}
          </section>
        </div>
        <div className="col-lg-4">
          <section className="card p-3">
            <h2 className="mb-3" style={{ fontSize: "1rem" }}>Reporting citizen</h2>
            <div className="fw-medium">{report.ownerName}</div>
            {report.ownerPhone && <a className="d-block mt-2" href={`tel:${report.ownerPhone}`}>{report.ownerPhone}</a>}
            {report.ownerEmail && <a className="d-block mt-1" href={`mailto:${report.ownerEmail}`}>{report.ownerEmail}</a>}
            <div className="text-muted-soft mt-3" style={{ fontSize: "0.78rem" }}>
              Submitted {formatDate(report.createdAt)}
            </div>
          </section>
        </div>
      </div>
    </AppLayout>
  );
}