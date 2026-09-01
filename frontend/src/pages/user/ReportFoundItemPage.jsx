import React, { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { categoryService } from "../../services/categoryService";
import { foundReportService } from "../../services/foundReportService";
import { extractErrorMessage } from "../../services/apiClient";

const today = new Date().toISOString().slice(0, 10);

export default function ReportFoundItemPage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ categoryId: "", description: "", brand: "", color: "", foundDate: today });
  const [location, setLocation] = useState({ city: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [done, setDone] = useState(null);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(() => {});
  }, []);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!location.latitude || !location.longitude || !location.city) {
      setError("Please enter the city and pick the approximate location on the map.");
      return;
    }
    setSubmitting(true);
    try {
      const res = await foundReportService.create({ ...form, location });
      setDone(res);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (done) {
    return (
      <AppLayout>
        <PageHeader eyebrow="Citizen · Found item" title="Report received" />
        <div className="card p-4" style={{ maxWidth: 640 }}>
          <p>
            Thank you — your report has been submitted. This is a preliminary record; please take the item to
            your nearest police station so an officer can take it into official custody and verify it.
          </p>
          <p className="text-muted-soft" style={{ fontSize: "0.85rem" }}>
            Reference: <span className="font-mono">{done.foundReportId}</span>
          </p>
          <Link to="/dashboard" className="btn btn-primary">
            Back to dashboard
          </Link>
        </div>
      </AppLayout>
    );
  }

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Citizen · Found item"
        title="Report a found item"
        subtitle="This creates a preliminary record. A police officer must still take the item into custody and verify it before it appears in search."
      />
      <form onSubmit={handleSubmit} className="card p-4" style={{ maxWidth: 720 }}>
        <ErrorAlert message={error} />

        <div className="mb-3">
          <label className="form-label">Category</label>
          <select className="form-select" required value={form.categoryId} onChange={update("categoryId")}>
            <option value="">Select a category…</option>
            {categories.map((c) => (
              <option key={c.categoryId} value={c.categoryId}>
                {c.categoryName}
              </option>
            ))}
          </select>
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
          {submitting ? "Submitting…" : "Submit report"}
        </button>
      </form>
    </AppLayout>
  );
}
