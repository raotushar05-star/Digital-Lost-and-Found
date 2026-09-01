import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { stationService } from "../../services/stationService";
import { extractErrorMessage } from "../../services/apiClient";

const emptyForm = { stationName: "", stationCode: "", address: "", phone: "", location: { city: "" } };

export default function StationManagementPage() {
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    stationService.getStations().then(setStations).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const startEdit = (station) => {
    setEditingId(station.stationId);
    setForm({
      stationName: station.stationName,
      stationCode: station.stationCode,
      address: station.address,
      phone: station.phone || "",
      location: station.location || { city: "" },
      isActive: station.isActive
    });
  };

  const resetForm = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (editingId) {
        await stationService.update(editingId, form);
      } else {
        await stationService.create(form);
      }
      resetForm();
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader eyebrow="Admin" title="Police stations" />
      <div className="row g-4">
        <div className="col-lg-7">
          {loading ? (
            <LoadingSpinner />
          ) : (
            <div className="card">
              <div className="list-group list-group-flush">
                {stations.map((s) => (
                  <div key={s.stationId} className="list-group-item d-flex justify-content-between align-items-center p-3">
                    <div>
                      <div className="fw-medium">{s.stationName}</div>
                      <div className="text-muted-soft font-mono" style={{ fontSize: "0.78rem" }}>{s.stationCode}</div>
                      <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>{s.address}</div>
                    </div>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => startEdit(s)}>
                      Edit
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className="col-lg-5">
          <form onSubmit={handleSubmit} className="card p-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">{editingId ? "Edit station" : "New station"}</h2>
            <ErrorAlert message={error} />
            <div className="mb-2">
              <label className="form-label">Station name</label>
              <input
                className="form-control"
                required
                value={form.stationName}
                onChange={(e) => setForm({ ...form, stationName: e.target.value })}
              />
            </div>
            {!editingId && (
              <div className="mb-2">
                <label className="form-label">Station code</label>
                <input
                  className="form-control"
                  required
                  value={form.stationCode}
                  onChange={(e) => setForm({ ...form, stationCode: e.target.value })}
                />
              </div>
            )}
            <div className="mb-2">
              <label className="form-label">Address</label>
              <textarea
                className="form-control"
                rows={2}
                required
                value={form.address}
                onChange={(e) => setForm({ ...form, address: e.target.value })}
              />
            </div>
            <div className="mb-2">
              <label className="form-label">Phone</label>
              <input className="form-control" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </div>
            <div className="mb-3">
              <label className="form-label">City</label>
              <input
                className="form-control mb-2"
                value={form.location?.city || ""}
                onChange={(e) => setForm({ ...form, location: { ...form.location, city: e.target.value } })}
              />
              <LocationPicker value={form.location} onChange={(loc) => setForm({ ...form, location: loc })} height={220} />
            </div>
            {editingId && (
              <div className="form-check mb-3">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id="stationActive"
                  checked={!!form.isActive}
                  onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
                />
                <label className="form-check-label" htmlFor="stationActive">Active</label>
              </div>
            )}
            <div className="d-flex gap-2">
              <button className="btn btn-primary btn-sm" disabled={submitting}>
                {submitting ? "Saving…" : editingId ? "Save changes" : "Create station"}
              </button>
              {editingId && (
                <button type="button" className="btn btn-outline-secondary btn-sm" onClick={resetForm}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </AppLayout>
  );
}
