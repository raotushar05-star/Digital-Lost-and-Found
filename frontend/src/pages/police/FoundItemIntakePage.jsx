import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { categoryService } from "../../services/categoryService";
import { stationService } from "../../services/stationService";
import { policeService } from "../../services/policeService";
import { foundReportService } from "../../services/foundReportService";
import { useAuth } from "../../context/AuthContext.jsx";
import { extractErrorMessage } from "../../services/apiClient";

const today = new Date().toISOString().slice(0, 10);

export default function FoundItemIntakePage() {
  const navigate = useNavigate();
  const { profile } = useAuth();
  const [searchParams] = useSearchParams();
  const foundReportId = searchParams.get("foundReportId");

  const [categories, setCategories] = useState([]);
  const [stations, setStations] = useState([]);
  const [form, setForm] = useState({
    stationId: "",
    categoryId: "",
    description: "",
    brand: "",
    color: "",
    privateIdentifyingDetails: "",
    foundDate: today
  });
  const [location, setLocation] = useState({ city: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(() => {});
    stationService.getStations().then(setStations).catch(() => {});
  }, []);

  useEffect(() => {
    if (profile?.stationId) {
      setForm((f) => ({ ...f, stationId: profile.stationId }));
    }
  }, [profile]);

  useEffect(() => {
    if (!foundReportId) return;
    foundReportService.getById(foundReportId).then((report) => {
      setForm((f) => ({
        ...f,
        categoryId: report.category ? f.categoryId : f.categoryId,
        description: report.description || f.description,
        brand: report.brand || f.brand,
        color: report.color || f.color,
        foundDate: report.foundDate || f.foundDate
      }));
      if (report.location) setLocation(report.location);
    });
  }, [foundReportId]);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!location.latitude || !location.longitude || !location.city) {
      setError("Please enter the city and pick the location on the map.");
      return;
    }
    setSubmitting(true);
    try {
      const res = await policeService.intakeFoundItem({
        ...form,
        foundReportId: foundReportId || null,
        location
      });
      navigate(`/police/found-items/${res.foundItemId}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Custody intake"
        title="Register a found item"
        subtitle="Only an authorized officer may create the official custody record."
      />
      <form onSubmit={handleSubmit} className="card p-4" style={{ maxWidth: 760 }}>
        <ErrorAlert message={error} />

        <div className="row g-2 mb-3">
          <div className="col-md-6">
            <label className="form-label">Station</label>
            <select className="form-select" required value={form.stationId} onChange={update("stationId")}>
              <option value="">Select station…</option>
              {stations.map((s) => (
                <option key={s.stationId} value={s.stationId}>
                  {s.stationName}
                </option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Category</label>
            <select className="form-select" required value={form.categoryId} onChange={update("categoryId")}>
              <option value="">Select category…</option>
              {categories.map((c) => (
                <option key={c.categoryId} value={c.categoryId}>
                  {c.categoryName}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Description</label>
          <textarea className="form-control" rows={3} required value={form.description} onChange={update("description")} />
        </div>

        <div className="row g-2 mb-3">
          <div className="col-md-6">
            <label className="form-label">Brand (optional)</label>
            <input className="form-control" value={form.brand} onChange={update("brand")} />
          </div>
          <div className="col-md-6">
            <label className="form-label">Color (optional)</label>
            <input className="form-control" value={form.color} onChange={update("color")} />
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Private identifying details</label>
          <textarea
            className="form-control"
            rows={2}
            placeholder="Restricted fields used only for ownership verification — never shown publicly"
            value={form.privateIdentifyingDetails}
            onChange={update("privateIdentifyingDetails")}
          />
        </div>

        <div className="mb-3">
          <label className="form-label">Date found</label>
          <input type="date" className="form-control" required max={today} value={form.foundDate} onChange={update("foundDate")} />
        </div>

        <div className="mb-3">
          <label className="form-label">City</label>
          <input
            className="form-control mb-2"
            required
            value={location.city || ""}
            onChange={(e) => setLocation({ ...location, city: e.target.value })}
          />
          <LocationPicker value={location} onChange={setLocation} />
        </div>

        <button className="btn btn-primary" disabled={submitting}>
          {submitting ? "Registering…" : "Register item"}
        </button>
      </form>
    </AppLayout>
  );
}
