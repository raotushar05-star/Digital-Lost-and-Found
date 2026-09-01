import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { caseService } from "../../services/caseService";
import { formatDateTime } from "../../utils/format";
import { getStatusMeta } from "../../utils/statusConfig";

export default function CaseTrackingPage() {
  const { id } = useParams();
  const [caseData, setCaseData] = useState(null);
  const [history, setHistory] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([caseService.getById(id), caseService.getHistory(id)])
      .then(([c, h]) => {
        setCaseData(c);
        setHistory(h);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading || !caseData) return <AppLayout><LoadingSpinner /></AppLayout>;

  return (
    <AppLayout>
      <div className="mb-4">
        <div className="page-eyebrow mb-1">
          {caseData.caseType === "LOST" ? "Lost item case" : "Found item case"}
        </div>
        <div className="d-flex align-items-center gap-2">
          <h1 style={{ fontSize: "1.5rem" }} className="mb-0 font-mono">
            {caseData.caseNumber}
          </h1>
          <StatusStamp status={caseData.currentStatus} />
        </div>
      </div>

      <div className="card p-4" style={{ maxWidth: 640 }}>
        <div className="page-eyebrow mb-3">Case progression</div>
        <div className="ledger-timeline">
          {history.history.map((entry, idx) => {
            const meta = getStatusMeta(entry.newStatus);
            const isLast = idx === history.history.length - 1;
            return (
              <div className={`ledger-step is-done`} key={idx}>
                <div className="ledger-dot" />
                <div>
                  <div className="ledger-label">{meta.label}</div>
                  {entry.remarks && (
                    <div className="text-muted-soft" style={{ fontSize: "0.82rem" }}>{entry.remarks}</div>
                  )}
                  <div className="ledger-meta">{formatDateTime(entry.changedAt)}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </AppLayout>
  );
}
