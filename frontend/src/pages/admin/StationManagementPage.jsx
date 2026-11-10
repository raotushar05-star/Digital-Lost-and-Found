import React, { useEffect, useRef, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import PasswordInput from "../../components/common/PasswordInput.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { stationService } from "../../services/stationService";
import { extractErrorMessage } from "../../services/apiClient";
import { addressError, cleanAddress } from "../../utils/validators";
import { reverseGeocode } from "../../utils/geocode";
import SuccessAlert from "../../components/common/SuccessAlert.jsx";

const STATION_NAME_RE = /^[\p{L}][\p{L} .'&()-]{2,99}$/u;
const PERSON_NAME_RE = /^[\p{L}][\p{L} .'-]{1,99}$/u;
const CODE_RE = /^[A-Z0-9-]{3,30}$/;
const OFFICER_PHONE_RE = /^[0-9]{10}$/;
const STATION_PHONE_RE = /^[0-9]{8,12}$/;
const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/;
const PASSWORD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,64}$/;

const emptyForm = {
  stationName: "",
  stationCode: "",
  address: "",
  phone: "",
  location: { city: "" },
  officerName: "",
  officerEmail: "",
  officerPhone: "",
  officerPassword: "",
  confirmPassword: ""
};

function validate(f, creating, geo) {
  const e = {};
  const name = f.stationName.trim();
  if (!STATION_NAME_RE.test(name)) e.stationName = "Letters and spaces only (at least 3 characters).";
  else if (creating && !/\bpolice station$/i.test(name)) e.stationName = "Name must end with 'Police Station', e.g. Manipal Police Station.";
  if (creating && !CODE_RE.test(f.stationCode)) e.stationCode = "3 to 30 characters: capital letters, numbers and hyphens.";
  const addrErr = addressError(f.address);
  if (addrErr) e.address = addrErr;
  if (f.phone && !STATION_PHONE_RE.test(f.phone)) e.phone = "Digits only, 8 to 12 digits (or leave empty).";
  if (f.location?.latitude == null || f.location?.longitude == null) e.pin = "Click the map to drop a pin on the station's real location.";
  else if (geo.status === "loading") e.pin = "Looking up the area…";
  else if (geo.status === "error") e.pin = "Could not identify this place. Click a different point on the map.";
  else if (f.location._country && f.location._country !== "in") e.pin = "The pin must be inside India.";
  else if (!f.location.city) e.pin = "No town or city found at this point. Click a nearby point.";
  if (creating) {
    if (!PERSON_NAME_RE.test(f.officerName.trim())) e.officerName = "Letters and spaces only.";
    if (!EMAIL_RE.test(f.officerEmail.trim())) e.officerEmail = "Enter a valid email like name@example.com.";
    if (!OFFICER_PHONE_RE.test(f.officerPhone)) e.officerPhone = "Exactly 10 digits, numbers only.";
    if (!PASSWORD_RE.test(f.officerPassword)) e.officerPassword = "8 to 64 characters with an uppercase letter, a lowercase letter, a number and a symbol.";
    if (f.confirmPassword !== f.officerPassword) e.confirmPassword = "Passwords do not match.";
  }
  return e;
}

function TextField({ label, error, className = "", ...props }) {
  return (
    <div className="mb-2">
      <label className="form-label">{label}</label>
      <input className={`form-control ${error ? "is-invalid" : ""} ${className}`} {...props} />
      {error && <div className="invalid-feedback d-block">{error}</div>}
    </div>
  );
}

export default function StationManagementPage() {
  const [stations, setStations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [attempted, setAttempted] = useState(false);
  const [geo, setGeo] = useState({ status: "idle" });
  const [unassigned, setUnassigned] = useState(null);
  const geoRequest = useRef(0);

  const load = () => {
    setLoading(true);
    stationService.getStations().then(setStations).finally(() => setLoading(false));
    stationService.getUnassigned().then(setUnassigned).catch(() => {});
  };

  useEffect(load, []);

  const errors = validate(form, !editingId, geo);
  const msg = (key, value) => (errors[key] && (attempted || value) ? errors[key] : null);

  const set = (field, clean = (v) => v) => (e) => setForm({ ...form, [field]: clean(e.target.value) });
  const digits = (v) => v.replace(/\D/g, "");
  const lettersOnly = (v) => v.replace(/[^\p{L} .'-]/gu, "");
  const stationNameChars = (v) => v.replace(/[^\p{L} .'&()-]/gu, "");

  const handlePin = async (picked) => {
    const id = ++geoRequest.current;
    setForm((f) => ({
      ...f,
      location: { latitude: picked.latitude, longitude: picked.longitude, city: "", state: "", postalCode: "", locality: "", _country: null }
    }));
    setGeo({ status: "loading" });
    try {
      const place = await reverseGeocode(picked.latitude, picked.longitude);
      if (id !== geoRequest.current) return; // a newer click replaced this one
      setForm((f) => ({
        ...f,
        address: f.address.trim() ? f.address : cleanAddress(place.displayName).slice(0, 250),
        location: {
          ...f.location,
          city: place.city,
          state: place.state,
          postalCode: place.postalCode,
          locality: place.locality,
          _country: place.country
        }
      }));
      setGeo({ status: "ok" });
    } catch {
      if (id !== geoRequest.current) return;
      setGeo({ status: "error" });
    }
  };

  const startEdit = (station) => {
    geoRequest.current++;
    setGeo({ status: "idle" });
    setEditingId(station.stationId);
    setSuccess(null);
    setError(null);
    setAttempted(false);
    setForm({
      ...emptyForm,
      stationName: station.stationName,
      stationCode: station.stationCode,
      address: station.address,
      phone: (station.phone || "").replace(/\D/g, ""),
      location: station.location || { city: "" },
      isActive: station.isActive
    });
  };

  const resetForm = () => {
    geoRequest.current++;
    setGeo({ status: "idle" });
    setEditingId(null);
    setForm(emptyForm);
    setAttempted(false);
  };

  const cleanLocation = (loc) => ({
    latitude: loc.latitude,
    longitude: loc.longitude,
    city: loc.city,
    state: loc.state || undefined,
    postalCode: loc.postalCode || undefined,
    locality: loc.locality || undefined
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setAttempted(true);
    if (Object.keys(errors).length > 0) {
      setError("Please fix the highlighted fields.");
      return;
    }
    setSubmitting(true);
    try {
      if (editingId) {
        await stationService.update(editingId, {
          stationName: form.stationName.trim(),
          address: cleanAddress(form.address),
          phone: form.phone,
          location: cleanLocation(form.location),
          isActive: form.isActive
        });
        setSuccess("Station updated.");
      } else {
        const { confirmPassword, ...payload } = form;
        const result = await stationService.create({
          ...payload,
          stationName: form.stationName.trim(),
          address: cleanAddress(form.address),
          location: cleanLocation(form.location),
          officerName: form.officerName.trim(),
          officerEmail: form.officerEmail.trim()
        });
        setSuccess(`Station created. The officer signs in as ${result.policeOfficer?.email} with the password you set.`);
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
      <PageHeader
        eyebrow="Admin"
        title="Police stations"
        subtitle="Creating a station also creates its Station Officer login. Lost reports are assigned to the nearest station with a map pin."
      />
      <div className="row g-4">
                <div className="col-lg-7">
          {loading ? (
            <LoadingSpinner />
          ) : (
            <>
              {unassigned && unassigned.lostReports + unassigned.foundReports > 0 && (
                <div className="alert alert-warning" style={{ fontSize: "0.85rem" }}>
                  {unassigned.lostReports} lost and {unassigned.foundReports} found report(s) are farther than the routing
                  limit from every station. Add a station near them and they will be assigned automatically.
                </div>
              )}
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
            </>
          )}
        </div>
        <div className="col-lg-5">
          <form onSubmit={handleSubmit} className="card p-3" autoComplete="off" noValidate>
            <h2 style={{ fontSize: "1rem" }} className="mb-2">{editingId ? "Edit station" : "New station"}</h2>
            <ErrorAlert message={error} />
            {success && <div className="alert alert-success py-2" style={{ fontSize: "0.85rem" }}>{success}</div>}

            <TextField
              label="Station name"
              placeholder={editingId ? "" : "e.g. Manipal Police Station"}
              value={form.stationName}
              onChange={set("stationName", stationNameChars)}
              error={msg("stationName", form.stationName)}
            />
            {!editingId && (
              <TextField
                label="Station code"
                placeholder="e.g. PS-MGL-002"
                value={form.stationCode}
                onChange={set("stationCode", (v) => v.toUpperCase().replace(/[^A-Z0-9-]/g, ""))}
                error={msg("stationCode", form.stationCode)}
              />
            )}
            <div className="mb-2">
              <label className="form-label">Address</label>
              <textarea
                className={`form-control ${msg("address", form.address) ? "is-invalid" : ""}`}
                rows={2}
                value={form.address}
                onChange={set("address")}
              />
              {msg("address", form.address) && <div className="invalid-feedback d-block">{errors.address}</div>}
            </div>
            <TextField
              label="Phone (optional)"
              inputMode="numeric"
              maxLength={12}
              value={form.phone}
              onChange={set("phone", digits)} 
              error={msg("phone", form.phone)}
            />

            <div className="mb-3">
              <label className="form-label">Location: click the station's real position on the map</label>
              <LocationPicker value={form.location} onChange={handlePin} height={220} />
              <div className="mt-2" style={{ fontSize: "0.82rem" }}>
                {geo.status === "loading" && <span className="text-muted-soft">Looking up the area…</span>}
                {form.location?.city && geo.status !== "loading" && (
                  <span>
                    <strong>Area:</strong>{" "}
                    {[form.location.locality, form.location.city, form.location.state, form.location.postalCode]
                      .filter(Boolean)
                      .join(", ")}
                  </span>
                )}
              </div>
              <input
                className="form-control form-control-sm mt-1"
                readOnly
                tabIndex={-1}
                placeholder="City (filled from the map pin)"
                value={form.location?.city || ""}
              />
              {(attempted || form.location?.latitude != null) && errors.pin && (
                <div className="text-danger mt-1" style={{ fontSize: "0.75rem" }}>{errors.pin}</div>
              )}
            </div>

            {!editingId && (
              <div className="border-top pt-3 mb-3">
                <h3 style={{ fontSize: "0.95rem" }} className="mb-1">Station Officer login</h3>
                <p className="text-muted-soft mb-2" style={{ fontSize: "0.8rem" }}>
                  The officer signs in with this email and password to handle reports, verification, claims and handovers.
                </p>
                <TextField
                  label="Name"
                  value={form.officerName}
                  onChange={set("officerName", lettersOnly)}
                  error={msg("officerName", form.officerName)}
                />
                <TextField
                  label="Email"
                  type="email"
                  autoComplete="off"
                  value={form.officerEmail}
                  onChange={set("officerEmail")}
                  error={msg("officerEmail", form.officerEmail)}
                />
                <TextField
                  label="Phone (10 digits)"
                  inputMode="numeric"
                  maxLength={10}
                  value={form.officerPhone}
                  onChange={set("officerPhone", digits)}
                  error={msg("officerPhone", form.officerPhone)}
                />
                <div className="mb-2">
                  <label className="form-label">Password</label>
                  <PasswordInput
                    autoComplete="new-password"
                    value={form.officerPassword}
                    onChange={set("officerPassword")}
                    error={msg("officerPassword", form.officerPassword)}
                  />
                  <div className="text-muted-soft" style={{ fontSize: "0.75rem" }}>
                    8+ characters with an uppercase letter, a lowercase letter, a number and a symbol.
                  </div>
                </div>
                <div className="mb-2">
                  <label className="form-label">Confirm password</label>
                  <PasswordInput
                    autoComplete="new-password"
                    value={form.confirmPassword}
                    onChange={set("confirmPassword")}
                    error={msg("confirmPassword", form.confirmPassword)}
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