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
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Analytics"
        title="Reports"
        actions={
          <form
            className="d-flex gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              load();
            }}
          >
            <input type="date" className="form-control form-control-sm" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
            <input type="date" className="form-control form-control-sm" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
            <button className="btn btn-sm btn-outline-primary">Apply</button>
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
