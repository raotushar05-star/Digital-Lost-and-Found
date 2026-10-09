import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import { policeService } from "../../services/policeService";
import { useAuth } from "../../context/AuthContext.jsx";

const SUMMARY_TILES = [
  ["lostItems", "Lost items reported"],
  ["foundItems", "Found items registered"],
  ["verifiedItems", "Items verified"],
  ["claims", "Claims filed"],
  ["recoveredItems", "Items recovered"]
];

export default function PoliceReportsPage() {
  const { isAdmin } = useAuth();
  const [summary, setSummary] = useState(null);
  const [stationReports, setStationReports] = useState([]);
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [appliedRange, setAppliedRange] = useState({ dateFrom: "", dateTo: "" });
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    const params = {};
    if (dateFrom) params.dateFrom = dateFrom;
    if (dateTo) params.dateTo = dateTo;
    const calls = [policeService.getReportsSummary(params)];
    if (isAdmin) calls.push(policeService.getStationReports(params));
    Promise.all(calls)
      .then(([s, st]) => {
        setSummary(s);
        if (st) setStationReports(st);
        setAppliedRange({ dateFrom, dateTo });
      })
      .finally(() => setLoading(false));
  };

  const downloadPdf = async () => {
    if (!summary) return;

    const [{ jsPDF }, { default: autoTable }] = await Promise.all([
      import("jspdf"),
      import("jspdf-autotable")
    ]);
    const document = new jsPDF({ format: "a4", unit: "pt" });
    const margin = 42;
    const pageWidth = document.internal.pageSize.getWidth();
    const generatedAt = new Date().toLocaleString();
    const period = appliedRange.dateFrom || appliedRange.dateTo
      ? `${appliedRange.dateFrom || "Start"} to ${appliedRange.dateTo || "Present"}`
      : "All dates";

    document.setFont("helvetica", "bold");
    document.setFontSize(18);
    document.text("FindLine - Police Reports", margin, 48);
    document.setFont("helvetica", "normal");
    document.setFontSize(10);
    document.text(`Reporting period: ${period}`, margin, 68);
    document.text(`Generated: ${generatedAt}`, margin, 84);

    autoTable(document, {
      startY: 104,
      head: [["Measure", "Count / rate"]],
      body: [
        ["Lost items reported", String(summary.lostItems)],
        ["Found items registered", String(summary.foundItems)],
        ["Items verified", String(summary.verifiedItems)],
        ["Claims filed", String(summary.claims)],
        ["Items recovered", String(summary.recoveredItems)],
        ["Recovery rate", `${summary.recoveryRate}%`]
      ],
      margin: { left: margin, right: margin },
      headStyles: { fillColor: [25, 101, 95] },
      styles: { font: "helvetica", fontSize: 9, cellPadding: 6 }
    });

    if (isAdmin) {
      const stationStartY = (document.lastAutoTable?.finalY || 104) + 24;
      document.setFont("helvetica", "bold");
      document.setFontSize(12);
      document.text("Station-wise report", margin, stationStartY);
      autoTable(document, {
        startY: stationStartY + 8,
        head: [["Station", "Found", "Verified", "Claims", "Recovered", "Recovery rate"]],
        body: stationReports.length
          ? stationReports.map((station) => [
              station.stationName,
              String(station.foundItems),
              String(station.verifiedItems),
              String(station.claims),
              String(station.recoveredItems),
              `${station.recoveryRate}%`
            ])
          : [["No station data for this period", "-", "-", "-", "-", "-"]],
        margin: { left: margin, right: margin },
        headStyles: { fillColor: [25, 101, 95] },
        styles: { font: "helvetica", fontSize: 8, cellPadding: 5 },
        columnStyles: { 0: { cellWidth: 160 } }
      });
    }

    const pageCount = document.internal.getNumberOfPages();
    for (let page = 1; page <= pageCount; page += 1) {
      document.setPage(page);
      document.setFont("helvetica", "normal");
      document.setFontSize(8);
      document.text(`Page ${page} of ${pageCount}`, pageWidth - margin, 810, { align: "right" });
    }

    const from = appliedRange.dateFrom || "all";
    const to = appliedRange.dateTo || "dates";
    document.save(`findline-police-report-${from}-to-${to}.pdf`);
  };

  useEffect(load, []);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Analytics"
        title="Reports"
        actions={
          <form
            className="d-flex gap-2 align-items-end"
            onSubmit={(e) => {
              e.preventDefault();
              load();
            }}
          >
            <div>
              <label htmlFor="reports-date-from" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>From</label>
              <input id="reports-date-from" type="date" className="form-control form-control-sm" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
            </div>
            <div>
              <label htmlFor="reports-date-to" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>To</label>
              <input id="reports-date-to" type="date" className="form-control form-control-sm" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
            </div>
            <button className="btn btn-sm btn-outline-primary">Apply</button>
            <button type="button" className="btn btn-sm btn-primary" disabled={loading || !summary} onClick={downloadPdf}>
              Download PDF
            </button>
          </form>
        }
      />
      {loading ? (
        <LoadingSpinner />
      ) : (
        <>
          <div className="row g-3 mb-4">
            {SUMMARY_TILES.map(([key, label]) => (
              <div className="col-md-4 col-lg-2" key={key}>
                <div className="card p-3 text-center">
                  <div className="font-mono" style={{ fontSize: "1.6rem", fontWeight: 600 }}>{summary[key]}</div>
                  <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>{label}</div>
                </div>
              </div>
            ))}
            <div className="col-md-4 col-lg-2">
              <div className="card p-3 text-center" style={{ background: "var(--brand-tint)" }}>
                <div className="font-mono text-brand" style={{ fontSize: "1.6rem", fontWeight: 600 }}>
                  {summary.recoveryRate}%
                </div>
                <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>Recovery rate</div>
              </div>
            </div>
          </div>

          {isAdmin && stationReports.length > 0 && (
            <div className="card p-3">
              <h2 style={{ fontSize: "1rem" }} className="mb-3">By station</h2>
              <div className="table-responsive">
                <table className="table table-sm align-middle">
                  <thead>
                    <tr className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                      <th>Station</th>
                      <th>Found items</th>
                      <th>Verified</th>
                      <th>Claims</th>
                      <th>Recovered</th>
                      <th>Recovery rate</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stationReports.map((s) => (
                      <tr key={s.stationId}>
                        <td className="fw-medium">{s.stationName}</td>
                        <td>{s.foundItems}</td>
                        <td>{s.verifiedItems}</td>
                        <td>{s.claims}</td>
                        <td>{s.recoveredItems}</td>
                        <td className="font-mono">{s.recoveryRate}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </AppLayout>
  );
}
