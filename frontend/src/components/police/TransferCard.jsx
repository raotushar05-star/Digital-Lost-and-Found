import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ErrorAlert from "../common/ErrorAlert.jsx";
import { policeService } from "../../services/policeService";
import { stationService } from "../../services/stationService";
import { extractErrorMessage } from "../../services/apiClient";
import { formatDateTime } from "../../utils/format";

export default function TransferCard({ item }) {
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [history, setHistory] = useState([]);
  const [toStationId, setToStationId] = useState("");
  const [reason, setReason] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    stationService.getStations().then(setStations).catch(() => {});
    policeService.getTransfers(item.foundItemId).then(setHistory).catch(() => {});
  }, [item.foundItemId]);

  const options = stations.filter((s) => s.isActive && s.stationId !== item.stationId);

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    const station = options.find((s) => s.stationId === toStationId);
    if (!window.confirm(`Transfer this item to ${station?.stationName}? It will leave your station's inventory.`)) return;
    setBusy(true);
    try {
      await policeService.transferItem(item.foundItemId, { toStationId, reason: reason.trim() });
      navigate("/police/found-items"); // the item now belongs to the other station
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="card p-3 mt-3">
      <h2 style={{ fontSize: "1rem" }} className="mb-2">Station transfer</h2>
      {history.length > 0 && (
        <div className="mb-3">
          {history.map((t) => (
            <div key={t.transferId} className="text-muted-soft" style={{ fontSize: "0.8rem" }}>
              {formatDateTime(t.transferredAt)} · {t.fromStationName} → {t.toStationName} · {t.transferredByName} · {t.reason}
            </div>
          ))}
        </div>
      )}
      {item.custodyStatus !== "IN_CUSTODY" ? (
        <p className="text-muted-soft mb-0" style={{ fontSize: "0.85rem" }}>Only items in custody can be transferred.</p>
      ) : (
        <form onSubmit={submit}>
          <ErrorAlert message={error} />
          <div className="mb-2">
            <label className="form-label">Move to</label>
            <select className="form-select" required value={toStationId} onChange={(e) => setToStationId(e.target.value)}>
              <option value="">Select a station…</option>
              {options.map((s) => (
                <option key={s.stationId} value={s.stationId}>{s.stationName}</option>
              ))}
            </select>
          </div>
          <div className="mb-2">
            <label className="form-label">Reason</label>
            <textarea className="form-control" rows={2} required maxLength={500} value={reason} onChange={(e) => setReason(e.target.value)} />
          </div>
          <button className="btn btn-sm btn-outline-primary" disabled={busy || !toStationId || !reason.trim()}>
            {busy ? "Transferring…" : "Transfer item"}
          </button>
        </form>
      )}
    </div>
  );
}