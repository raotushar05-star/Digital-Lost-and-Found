import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { stationService } from "../../services/stationService";
import { extractErrorMessage } from "../../services/apiClient";

const emptyForm = {
  stationName: "",
  stationCode: "",
  address: "",
  phone: "",
  location: { city: "Mangaluru" },
  adminName: "",
  adminEmail: "",
  adminPhone: "",
  adminPassword: ""
};

export default function StationManagementPage() {
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    stationService.getStations().then(setStations).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const startEdit = (station) => {
    setEditingId(station.stationId);
    setSuccess(null);
    setForm({
      stationName: station.stationName,
      stationCode: station.stationCode,
      address: station.address,
      phone: station.phone || "",
      location: station.location || { city: "" },
      isActive: station.isActive,
      adminName: "",
      adminEmail: "",
      adminPhone: "",
      adminPassword: ""
    });
  };

  const resetForm = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setSubmitting(true);
    try {
      if (editingId) {
        await stationService.update(editingId, {
          stationName: form.stationName,
          address: form.address,
          phone: form.phone,
          location: form.location,
          isActive: form.isActive
        });
        setSuccess("Station updated.");
        resetForm();
      } else {
        const result = await stationService.create(form);
        setSuccess(
          `${result.message || "Station created."} Sign in as ${result.policeAdmin?.email} with the password you just set.`
        );
        resetForm();
      }
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Admin"
        title="Police stations"
        subtitle="Creating a station also provisions a police admin login for that station. Lost reports are assigned to the nearest station with a map pin."
      />
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
            <h2 style={{ fontSize: "1rem" }} className="mb-2">{editingId ? "Edit station" : "New station + login"}</h2>
            <ErrorAlert message={error} />
            {success && <div className="alert alert-success py-2" style={{ fontSize: "0.85rem" }}>{success}</div>}
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
                  placeholder="e.g. PS-MGL-002"
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
              <label className="form-label">City &amp; map pin (required for nearest-station routing)</label>
              <input
                className="form-control mb-2"
                required
                value={form.location?.city || ""}
                onChange={(e) => setForm({ ...form, location: { ...form.location, city: e.target.value } })}
              />
              <LocationPicker value={form.location} onChange={(loc) => setForm({ ...form, location: loc })} height={220} />
            </div>
            {!editingId && (
              <div className="border-top pt-3 mb-3">
                <h3 style={{ fontSize: "0.95rem" }} className="mb-2">Police admin sign-in</h3>
                <p className="text-muted-soft mb-2" style={{ fontSize: "0.8rem" }}>
                  Only you (System Admin) can create this login. Public registration cannot become police.
                </p>
                <div className="mb-2">
                  <label className="form-label">Admin name</label>
                  <input
                    className="form-control"
                    required
                    value={form.adminName}
                    onChange={(e) => setForm({ ...form, adminName: e.target.value })}
                  />
                </div>
                <div className="mb-2">
                  <label className="form-label">Admin email</label>
                  <input
                    type="email"
                    className="form-control"
                    required
                    placeholder="barke.admin@example.com"
                    value={form.adminEmail}
                    onChange={(e) => setForm({ ...form, adminEmail: e.target.value })}
                  />
                </div>
                <div className="mb-2">
                  <label className="form-label">Admin phone</label>
                  <input
                    className="form-control"
                    required
                    value={form.adminPhone}
                    onChange={(e) => setForm({ ...form, adminPhone: e.target.value })}
                  />
                </div>
                <div className="mb-2">
                  <label className="form-label">Admin password</label>
                  <input
                    type="password"
                    className="form-control"
                    required
                    minLength={8}
                    value={form.adminPassword}
                    onChange={(e) => setForm({ ...form, adminPassword: e.target.value })}
                  />
                </div>
              </div>
            )}
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
                {submitting ? "Saving…" : editingId ? "Save changes" : "Create station & login"}
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
